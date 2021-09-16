package com.vicegym.qrtrainertruck.data

import com.vicegym.qrtrainertruck.R

object MyUser {
    var id: String? = null
    var name: String? = null
    var email: String? = null
    var password: String? = null
    var mobile: String? = null
    var profilePicture: String = "android.resource://com.vicegym.qrtrainertruck/" + R.drawable.default_profpic
    var onlineProfilePictureUri: String? = null
    var acceptedTermsAndConditions: Boolean = false
    var rank: String = "Újonc"
    var score: Number = 0
    var nextTraining: TrainingData? = null
    //var trainingList: ArrayList<TrainingData> = arrayListOf()
}