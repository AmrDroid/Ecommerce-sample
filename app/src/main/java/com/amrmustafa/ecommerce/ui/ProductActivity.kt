package com.amrmustafa.ecommerce.ui


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amrmustafa.ecommerce.Injection
import com.amrmustafa.ecommerce.R
import com.amrmustafa.ecommerce.databinding.ActivityMainBinding

import com.amrmustafa.ecommerce.model.Products
import com.amrmustafa.ecommerce.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*


class ProductActivity : AppCompatActivity() {

    //this handle the viewBinding to avoid findByView
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    val searchState = MutableLiveData<Boolean>()


    //keep track of products list
    private var productLists: PagedList<Products>? = null

    //[ProductAdapter] field, am using backing scope because [ProductAdapter] accept context
    private var _productAdapter: ProductAdapter? = null
    private val productAdapter get() = _productAdapter!!

    /*
    viewModel for [ProductViewModel] class
     */
    private lateinit var viewModel: ProductViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // get the view model
        viewModel = ViewModelProvider(this, Injection.provideViewModelFactory(this)).get(
            ProductViewModel::class.java
        )


        //  getProductForSearchView()


        //add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(decoration)


        //if the call icon is click [callSeller] is called.
        call.setOnClickListener {
            it.callSeller()
        }


        chat.setOnClickListener {
            if (checkInternetAccess()) {
                chatSeller()
            } else {
                message("Check internet connection!!!")
            }

        }


        //function that handle retrieval of products list
        getProductList()


        /*
     action perform if retry is clicked,
    if there is internet access [refresh] function,
    else loading view visibility is set to gone & swipeRefreshing is set to false, so that the swipeRefresh dialog will be visible
      */
        retry.setOnClickListener {
            if (checkInternetAccess()) {
                retry.gone()
                refresh()

            } else {
                loading.gone()
                retry.visible()
            }

        }


        /*
        network error from [ProductResult]
         */
        viewModel.networkErrors.observe(this, Observer {
            Toast.makeText(this, "Wooops $it", Toast.LENGTH_SHORT).show()
            retry.visible()
            emptyList.text = getString(R.string.check_internet)

            loading.gone()
            swipeRefresh.isRefreshing = false

            //the retry visibility should be set to gone in as much the product list is not empty
            if (productLists!!.isNotEmpty())
                retry.gone()

        })

        //function that handle swipe refresh
        swipeAction()


        /**
         * this listener handle the searchview to know if focused or not
         * if focused nothing swipe refresh should set to false
         * else call [swipeAction] function
         */

        search_product.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchState.value = true
                swipeRefresh.setOnRefreshListener {
                    swipeRefresh.isRefreshing = false
                }
            } else {
                swipeAction()
                searchState.value = false

            }
        }


        fab.setOnClickListener { //          recyclerView.scrollToPosition(0);
            recyclerView.smoothScrollToPosition(0)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (dy > 0) { // scrolling down
                    fab.visibility = View.VISIBLE
                } else if (dy < 0) { // scrolling up
                    fab.visibility = View.GONE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { // No scrolling
                    fab.visibility = View.VISIBLE
                }
            }
        })





        binding.root
    }

    /**the [relLayout] is set to gone before swipe refresh is performed
     * if there is internet access the [refresh] function is called.
     * else loading is gone,swipe refreshing is set to false
     * toast is display to user
     */
    private fun swipeAction() {

        swipeRefresh.setOnRefreshListener {
            if (checkInternetAccess()) {
                refresh()

            } else {
                loading.gone()
                swipeRefresh.isRefreshing = false
                message("Check internet connection!!!")
            }


        }
    }

    /*
       retrieve [PagedList<Products>] via [ProductViewModel]
       if null is retrieve via [ProductViewModel] [showEmptyList] function will handle view to display
       else result is passed to [ProductAdapter]
       searchview should also visible the product list is not empty
        */
    private fun getProductList() {


        viewModel.productList.observe(this, Observer {
            _productAdapter = ProductAdapter(this)
            showEmptyList(it.size == 0)

            productLists = it
            productAdapter.submitList(it)
            initRecyclerview()
            initSearchView()

        })
    }

    private fun initRecyclerview() {
        recyclerView.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /*
    if the list of the products is empty, this function will handle the view to display
    the [visible] make view visibility to visible & [gone] make view visibility to gone
     */
    private fun showEmptyList(show: Boolean) {
        if (show) {
            emptyList.visible()
            recyclerView.gone()
            loading.visible()
            call.gone()
            chat.gone()
            binding.searchProduct.gone()
        } else {
            emptyList.gone()
            recyclerView.visible()
            call.visible()
            chat.visible()
            loading.gone()
            relLayout.gone()
            binding.searchProduct.visible()

        }
    }

    private fun refresh() {
        viewModel.networkStates.loadToRefresh();
        viewModel.networkStates.valueForRefresh.observe(this, Observer {
            if (it.status == NetworkState.LOADED.status) {
                swipeRefresh.isRefreshing = false
                productAdapter.notifyDataSetChanged()
                message("product updated successfully!!")
            }
            if (it.status == NetworkState.error("Internet error!, Check Internet Connection").status) {
                message("Internet error!, Check Internet Connection")
                swipeRefresh.isRefreshing = false
            }

        })
    }


    private fun initSearchView() {
        binding.searchProduct.addTextChangedListener(DebouncingQueryTextListener(
            this@ProductActivity.lifecycle
        ) { newText ->
            newText?.let {
                if (it.isEmpty()) {
                    getProductList()
                    relLayout.gone()

                } else {
                    getSearchResult(it)
                }
            }
        })

    }


    override fun onResume() {
        super.onResume()
        swipeAction()
    }


    internal class DebouncingQueryTextListener(
        lifecycle: Lifecycle,
        val onDebouncingQueryTextChange: (String?) -> Unit
    ) : TextWatcher, LifecycleObserver {
        var debouncePeriod: Long = 2000

        private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

        private var searchJob: Job? = null

        init {
            lifecycle.addObserver(this)
        }


        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun destroy() {
            searchJob?.cancel()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {


            searchJob?.cancel()
            searchJob = coroutineScope.launch {

                s.toString()?.let {
                    delay(debouncePeriod)

                    onDebouncingQueryTextChange(s.toString())
                }
            }

        }
    }

    fun getSearchResult(query: String) {


        swipeRefresh.isRefreshing = false
        relLayout.gone()
        // appending '%' so we can allow other characters to be before and after the query string
        viewModel.setFilterName("%${query}%")

        viewModel.dataList.observe(this@ProductActivity, Observer {
            productAdapter.submitList(null)


//            message(query)
            productAdapter.setQuery(query)
            productAdapter.submitList(it)

            //if product is not find base on the search keyword the [binding.relLayout] is visible
            //hide keyboard

            if (productAdapter.itemCount == 0) {

                //if the loading is visible the search error view should be gone
                if (loading.isVisible) {
                    relLayout.gone()
                }

                relLayout.visible()
                call.gone()
                chat.gone()

                please_try_again.text =
                    getString(R.string.error_message, query)
                try {
                    hideKeyboard()
                } catch (exc: Exception) {
                    exc.printStackTrace()
                }

            } else {
                relLayout.gone()
                call.visible()
                chat.visible()
            }
        })
    }


}
