package com.csc48.deliverycoffeeshop.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.bumptech.glide.Glide
import com.csc48.deliverycoffeeshop.R
import com.csc48.deliverycoffeeshop.model.ProductModel

class ProductsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mData: List<ProductModel> = listOf()

    private var callback: OnSelectListener? = null

    interface OnSelectListener

    fun setOnSelectListener(callback: OnSelectListener) {
        this.callback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_product_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bindViews(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context = itemView.context
        private val imvProductImage = itemView.findViewById<ImageView>(R.id.imvProductImage)
        private val tvProductName = itemView.findViewById<TextView>(R.id.tvProductName)
        private val tvProductPrice = itemView.findViewById<TextView>(R.id.tvProductPrice)
        private val swProductAvailable = itemView.findViewById<Switch>(R.id.swProductAvailable)

        init {
            swProductAvailable.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) swProductAvailable.text = "เปิดใช้งาน"
                else swProductAvailable.text = "ปิดใช้งาน"
            }
        }

        fun bindViews(productModel: ProductModel) {
            Glide.with(context)
                .load(productModel.image ?: "")
                .into(imvProductImage)

            tvProductName.text = productModel.name ?: ""
            tvProductPrice.text = productModel.price.toString()

            swProductAvailable.isChecked = productModel.available
        }
    }
}