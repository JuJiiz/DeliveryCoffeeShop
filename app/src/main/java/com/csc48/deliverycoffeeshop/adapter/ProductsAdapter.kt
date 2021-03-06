package com.csc48.deliverycoffeeshop.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.csc48.deliverycoffeeshop.R
import com.csc48.deliverycoffeeshop.model.ProductModel
import com.csc48.deliverycoffeeshop.utils.USER_ROLE_CUSTOMER


class ProductsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mData: List<ProductModel> = listOf()
    var userRole: Int = USER_ROLE_CUSTOMER

    private var customerCallback: OnProductCustomerListener? = null

    private var adminCallback: OnProductAdminListener? = null

    interface OnProductCustomerListener {
        fun onSelectItem(productModel: ProductModel)
    }

    interface OnProductAdminListener {
        fun onAvailableChange(productModel: ProductModel)
        fun onEditProduct(productModel: ProductModel)
        fun onDeleteProduct(productModel: ProductModel)
    }

    fun setOnSelectListener(callback: OnProductCustomerListener) {
        this.customerCallback = callback
    }

    fun setOnAvailableChangeListener(callback: OnProductAdminListener) {
        this.adminCallback = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (userRole != USER_ROLE_CUSTOMER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_product_manage_item, parent, false)
            AdminViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_product_choose_item, parent, false)
            CustomerViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (userRole != USER_ROLE_CUSTOMER) (holder as AdminViewHolder).bindViews(mData[position])
        else (holder as CustomerViewHolder).bindViews(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    internal inner class AdminViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context = itemView.context
        private val imvProductImage = itemView.findViewById<ImageView>(R.id.imvProductImage)
        private val tvProductName = itemView.findViewById<TextView>(R.id.tvProductName)
        private val tvProductPrice = itemView.findViewById<TextView>(R.id.tvProductPrice)
        private val swProductAvailable = itemView.findViewById<Switch>(R.id.swProductAvailable)
        private val btnMenu = itemView.findViewById<FrameLayout>(R.id.btnMenu)

        init {
            swProductAvailable.setOnClickListener {
                val isChecked = swProductAvailable.isChecked
                val productModel = mData[adapterPosition].apply {
                    update_at = System.currentTimeMillis()
                    available = isChecked
                }
                adminCallback?.onAvailableChange(productModel)
            }

            swProductAvailable.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) swProductAvailable.text = "เปิดใช้งาน"
                else swProductAvailable.text = "ปิดใช้งาน"
            }

            btnMenu.setOnClickListener {
                PopupMenu(context, it).apply {
                    inflate(R.menu.product_menu)
                    setOnMenuItemClickListener { menu ->
                        when (menu.itemId) {
                            R.id.menuEdit -> adminCallback?.onEditProduct(mData[adapterPosition])
                            R.id.menuDelete -> adminCallback?.onDeleteProduct(mData[adapterPosition])
                        }
                        false
                    }
                    show()
                }
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

    internal inner class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val context = itemView.context
        private val rootItem = itemView.findViewById<LinearLayout>(R.id.rootItem)
        private val imvProductImage = itemView.findViewById<ImageView>(R.id.imvProductImage)
        private val tvProductName = itemView.findViewById<TextView>(R.id.tvProductName)
        private val tvProductPrice = itemView.findViewById<TextView>(R.id.tvProductPrice)
        private val btnChooseProduct = itemView.findViewById<Button>(R.id.btnChooseProduct)

        init {
            btnChooseProduct.setOnClickListener {
                rootItem.isSelected = !rootItem.isSelected
                customerCallback?.onSelectItem(mData[adapterPosition])
            }
        }

        fun bindViews(productModel: ProductModel) {
            Glide.with(context)
                    .load(productModel.image ?: "")
                    .into(imvProductImage)

            tvProductName.text = productModel.name ?: ""
            tvProductPrice.text = productModel.price.toString()
        }
    }
}