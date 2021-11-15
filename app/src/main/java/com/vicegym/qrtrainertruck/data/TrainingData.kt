package com.vicegym.qrtrainertruck.data

data class TrainingData(
    var title: String? = null,
    var trainer: String? = null,
    var location: String? = null,
    var date: String? = null,
    var sorter: Long? = null,
    var trainees: ArrayList<String>? = null
)