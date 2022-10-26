package com.udacity.chatto.models

data class User(val email:String?=null,val image:String?=null,val name:String?=null,val users:List<String>?=null,val channel:List<String>?=null)
