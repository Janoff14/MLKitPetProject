package com.sanjarbek.mlkitfunproject

import java.net.IDN

class ProductModel(id: String, date: String, name: String) {
    private var pr_id: String = id
    private var pr_date: String = date
    private var pr_name: String = name

    fun get_id(): String{
        return pr_id
    }

    fun get_date(): String{
        return pr_date
    }

    fun get_name(): String{
        return pr_name
    }

    fun set_id(id: String){
        this.pr_id = id
    }

    fun set_date(date: String){
        this.pr_date = date
    }

    fun set_name(name: String){
        this.pr_name = name
    }

}