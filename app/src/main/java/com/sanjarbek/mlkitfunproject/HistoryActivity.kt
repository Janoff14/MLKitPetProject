package com.sanjarbek.mlkitfunproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import android.content.Intent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.camera.core.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val pr_list = ArrayList<ProductModel>()


        var name = ""
        var date = ""
        var id = ""
        db = Firebase.firestore
        val docRef = db.collection("products")
        docRef.get()
            .addOnSuccessListener { documents->
                for (document in documents){
                    name = document.data["product name"].toString()
                    date = document.data["product date"].toString()
                    id = document.data["product id"].toString()

                    val model = ProductModel(id, date, name)

                    pr_list.add(model)
                }
                Log.d("frag_1", "onViewCreated: ${pr_list.size}")
                val adapter = this.let { ProductAdapter(it,pr_list) }
                val recycler = findViewById<RecyclerView>(R.id.recycler_history)
                recycler.adapter = adapter
            }
            .addOnFailureListener {
                Log.d("TAG", "onViewCreated: failure")
            }

    }
}