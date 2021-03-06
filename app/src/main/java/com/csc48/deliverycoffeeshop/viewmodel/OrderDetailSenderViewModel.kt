package com.csc48.deliverycoffeeshop.viewmodel

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.csc48.deliverycoffeeshop.model.OrderModel
import com.csc48.deliverycoffeeshop.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class OrderDetailSenderViewModel @Inject constructor() : ViewModel() {
    private val TAG = OrderDetailSenderViewModel::class.java.simpleName
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    val order = MutableLiveData<OrderModel>()
    val user = MutableLiveData<UserModel>()

    private val userListener = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            Log.d(TAG, "getUser databaseError : $databaseError")
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            user.value = dataSnapshot.getValue(UserModel::class.java)
        }
    }

    fun getUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val ref = database.reference.child("users").child(currentUser.uid)
            ref.removeEventListener(userListener)
            ref.addValueEventListener(userListener)
        }
    }

    private val orderListener = object : ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            Log.d(TAG, "getOrder databaseError : $databaseError")
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            order.value = dataSnapshot.getValue(OrderModel::class.java)
        }
    }

    fun getOrder(orderKey: String?) {
        if (orderKey != null) {
            val ref = database.reference.child("orders").child(orderKey)
            ref.removeEventListener(orderListener)
            ref.addValueEventListener(orderListener)
        }
    }

    fun removeListener(orderKey: String?) {
        if (orderKey != null) {
            val ref = database.reference.child("orders").child(orderKey)
            ref.removeEventListener(orderListener)
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val ref = database.reference.child("users").child(currentUser.uid)
            ref.removeEventListener(userListener)
        }
    }

    fun updateOrderStatus(activity: Activity, orderKey: String?, status: Int) {
        if (orderKey != null) {
            val map = HashMap<String, Any>()
            map["status"] = status
            database.reference
                    .child("orders")
                    .child(orderKey)
                    .updateChildren(map)
                    .addOnSuccessListener {
                        activity.finish()
                    }
        }
    }
}