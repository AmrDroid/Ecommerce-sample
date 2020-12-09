package com.amrmustafa.ecommerce.ui


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amrmusafa.android.queryhighlight.text.format.QueryHighlighter
import com.bumptech.glide.Glide
import com.amrmustafa.ecommerce.R
import com.amrmustafa.ecommerce.databinding.LayoutProductBinding
import com.amrmustafa.ecommerce.model.Products


class ProductAdapter(private val context: Context) :
    PagedListAdapter<Products, ProductViewHolder>(DIFF_CALLBACK) {

    //this handle the viewBinding to avoid findByView
    private var _binding: LayoutProductBinding? = null
    private val binding get() = _binding!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_product, parent, false)
        _binding = LayoutProductBinding.bind(view)


        return ProductViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val product = getItem(position)

        //this listener handle event when the user tap the product,
        //this open a browser to display the product page from the website.
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(product!!.permalink_link)))
        }


        try {
            val mQueryHighlighter: QueryHighlighter

            mQueryHighlighter =
                QueryHighlighter().setQueryNormalizer(QueryHighlighter.QueryNormalizer.FOR_SEARCH)
            mQueryHighlighter.setText((holder.pName as TextView?)!!, getItem(position)!!.name, query)


            var description = product?.short_description

            //convert the product description to String, reason
            //description retrieve from webservice is in html format.
            //and support backward version of android


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                description = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY).toString()

            } else {
                description =
                    HtmlCompat.fromHtml(description!!, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

            }




            holder.shortDescription.text = description
            if (product!!.sale_price.isEmpty()) {
                product.sale_price = "0.0"
            }

            holder.actualPrice.text = product.regular_price +"$"
            holder.discountPrice.text = product.sale_price+"$"
            holder.actualPrice.paintFlags = holder.actualPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG


            //get list of images, then loop through to get the Image,
            //then retrieve the image link[String], which is used to by the glide library to display the image
            val images = product.images
            var imageValues: String? = null
            for (image in images) {
                imageValues = image.src
            }

            //glide library to handle the product image,
            //image_loading in drawable folder display if the product is not available
            Glide.with(context).load(imageValues).circleCrop().placeholder(R.drawable.image_loading)
                .into(holder.productImage)


            //if the product is outofstock the quantity should be 0
            //else display the quantity
            var quantity = product.stock_quantity.toString()
            if (product.stock_status.equals(context.getString(R.string.outofstock))) {
                quantity = "0"
                 holder.stockStatus.setTextColor(Color.parseColor("#ff9900"))
            }
            holder.quantity.text = "($quantity)"





            holder.stockStatus.text = product.stock_status
            holder.rating.rating = product.average_rating!!.toFloat()
            if (product.rating_count == 0) {
                holder.ratingCount.text = context.getString(R.string.no_review)
            } else {
                holder.ratingCount.text = "(${product.rating_count.toString()})"
            }


        } catch (exception: NullPointerException) {
            Log.e("ProductAdapter", "product is null")
        }


    }



    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<Products>() {
            // Product details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                oldConcert: Products,
                newConcert: Products
            ) = oldConcert.id == newConcert.id

            override fun areContentsTheSame(
                oldConcert: Products,
                newConcert: Products
            ) = oldConcert == newConcert
        }
    }

    var query = ""
    @JvmName("setQuery1")
    fun setQuery(q: String) {
        query = q
    }
}

class ProductViewHolder(binding: LayoutProductBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val pName = binding.pName
    val shortDescription = binding.description
    val actualPrice = binding.actualPrice

    val discountPrice = binding.discountPrice

    val productImage = binding.userImage
    val stockStatus: TextView = binding.status
    val rating = binding.productRating
    val ratingCount = binding.rateCount
    val quantity = binding.quantityValue
}
