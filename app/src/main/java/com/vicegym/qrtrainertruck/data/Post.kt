package com.vicegym.qrtrainertruck.data

data class Post(
    var authorId: String? = null,
    var authorName: String? = null,
    var title: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var sorter: Long? = null
)