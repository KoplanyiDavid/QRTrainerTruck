package com.vicegym.qrtrainertruck.data

import com.vicegym.qrtrainertruck.R

object myUser {
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var password: String? = null
    var mobile: String? = null
    var profilePicture: String = "android.resource://com.vicegym.qrtrainertruck/" + R.drawable.ic_profile
    var acceptedTermsAndConditions: Boolean = false
    var rank: String = "Újonc"
    var score: Number = 0
    var trainingList: MutableList<TrainingData> = mutableListOf()
}