package com.csc48.deliverycoffeeshop.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.csc48.deliverycoffeeshop.R
import com.csc48.deliverycoffeeshop.adapter.ProductsAdapter
import com.csc48.deliverycoffeeshop.model.OrderModel
import com.csc48.deliverycoffeeshop.model.ProductModel
import com.csc48.deliverycoffeeshop.model.UserModel
import com.csc48.deliverycoffeeshop.ui.fragment.*
import com.csc48.deliverycoffeeshop.viewmodel.ProductViewModel
import com.csc48.deliverycoffeeshop.viewmodel.ViewModelFactory
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_product.*
import javax.inject.Inject

class ProductActivity : AppCompatActivity()
    , HasSupportFragmentInjector
    , AdminConsoleDialogFragment.ConsoleListener
    , CustomerConsoleDialogFragment.ConsoleListener
    , AddCartFragment.AddCartListener
    , OrderCartDialogFragment.OrderCartListener
    , OrderEditorFragment.OrderEditorListener {
    private val TAG = ProductActivity::class.java.simpleName
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private lateinit var mViewModel: ProductViewModel
    private var adapter = ProductsAdapter()
    private var productData: List<ProductModel> = listOf()
    private var cart: List<ProductModel> = listOf()
    private var userModel: UserModel? = null

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(ProductViewModel::class.java)
        setContentView(R.layout.activity_product)

        rvProducts.layoutManager = GridLayoutManager(this, 2, LinearLayout.VERTICAL, false)
        rvProducts.setHasFixedSize(true)

        mViewModel.user.observe(this, Observer { user ->
            if (user != null) {
                rvProducts.adapter = null
                adapter = ProductsAdapter().apply {
                    isAdmin = user.is_admin
                    mData = productData
                }
                rvProducts.adapter = adapter

                if (user.is_admin) {
                    cart = listOf()
                    userModel = null
                    btnAdminConsole.visibility = View.VISIBLE
                    btnCustomerConsole.visibility = View.GONE
                    adapter.setOnAvailableChangeListener(object : ProductsAdapter.OnAvailableChangeListener {
                        override fun onAvailableChange(productModel: ProductModel) {
                            mViewModel.updateProduct(productModel, null)
                        }
                    })
                } else {
                    userModel = user
                    btnAdminConsole.visibility = View.GONE
                    btnCustomerConsole.visibility = View.VISIBLE
                    adapter.setOnSelectListener(object : ProductsAdapter.OnSelectListener {
                        override fun onSelectItem(productModel: ProductModel) {
                            if (supportFragmentManager.findFragmentByTag("AddCartFragment") == null) {
                                val bundle = Bundle().apply {
                                    putParcelable("PRODUCT", productModel)
                                }
                                val dialog = AddCartFragment().apply {
                                    arguments = bundle
                                    setAddCartListener(this@ProductActivity)
                                }
                                dialog.show(supportFragmentManager, "AddCartFragment")
                            }
                        }
                    })
                }
            }
        })
        mViewModel.getUser()

        mViewModel.products.observe(this, Observer { products ->
            productData = products ?: listOf()
            adapter.mData = productData
            adapter.notifyDataSetChanged()
        })
        mViewModel.getProducts()

        btnBack.setOnClickListener {
            this.finish()
        }

        btnAdminConsole.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("AdminConsoleDialogFragment") == null) {
                val dialog = AdminConsoleDialogFragment()
                dialog.setConsoleListener(this)
                dialog.show(supportFragmentManager, "AdminConsoleDialogFragment")
            }
        }

        btnCustomerConsole.setOnClickListener {
            if (supportFragmentManager.findFragmentByTag("CustomerConsoleDialogFragment") == null) {
                val dialog = CustomerConsoleDialogFragment()
                dialog.setConsoleListener(this)
                dialog.show(supportFragmentManager, "CustomerConsoleDialogFragment")
            }
        }
    }

    override fun onAddProduct() {
        if (supportFragmentManager.findFragmentByTag("ProductEditorDialogFragment") == null) {
            val dialog = ProductEditorDialogFragment()
            dialog.show(supportFragmentManager, "ProductEditorDialogFragment")
        }
    }

    override fun onManageUser() {
        val intent = Intent(this, UserManagementActivity::class.java)
        startActivity(intent)
    }

    override fun onCartManage() {
        if (supportFragmentManager.findFragmentByTag(OrderCartDialogFragment.TAG) == null) {
            if (!cart.isNullOrEmpty()) {
                val dialog = OrderCartDialogFragment.newInstance(cart)
                dialog.setOrderCartListener(this)
                dialog.show(supportFragmentManager, OrderCartDialogFragment.TAG)
            } else {
                Toast.makeText(this, "กรุณาเลือกสินค้า", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOrderManage() {
        val intent = Intent(this, OrderManagementActivity::class.java)
        startActivity(intent)
    }

    override fun onEditProfile() {
        val intent = Intent(this, UserInfoActivity::class.java)
        startActivity(intent)
    }

    override fun onLogout() {
        mViewModel.logout(this)
    }

    override fun onAddCart(productModel: ProductModel) {
        val hasData = cart.find { it.key == productModel.key }
        cart = if (hasData == null) cart + productModel else (cart - hasData) + productModel
    }

    override fun onClearCart() {
        cart = listOf()
        Toast.makeText(this, "เคลียร์ตะกร้าเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
    }

    override fun onOrderEditor() {
        if (supportFragmentManager.findFragmentByTag(OrderEditorFragment.TAG) == null) {
            val fragment = OrderEditorFragment.newInstance(userModel, cart)
            fragment.setOrderEditorListener(this)

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                .replace(android.R.id.content, fragment, OrderEditorFragment.TAG)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onCreateOrder(orderModel: OrderModel) {
        mViewModel.updateOrderResponse.observe(this, Observer { task ->
            if (task != null) {
                when {
                    task.isSuccessful -> {
                        Toast.makeText(this, "สร้างออเดอร์สำเร็จ", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, OrderManagementActivity::class.java)
                        startActivity(intent)
                    }
                    task.isCanceled -> {
                        Toast.makeText(this, "สร้างออเดอร์ไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "onCreateOrder error: ${task.exception?.message}")
                    }
                }
                if (mViewModel.updateOrderResponse.value != null) mViewModel.updateOrderResponse.value = null
            }
        })
        mViewModel.updateOrder(orderModel)
    }
}