package com.vicegym.qrtrainertruck.helpers

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.widget.ImageView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.vicegym.qrtrainertruck.data.MyUser
import com.vicegym.qrtrainertruck.otheractivities.BaseActivity
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

object FirebaseHelper : BaseActivity() {

    private val storage = Firebase.storage.reference
    var profilePictureUrl: Uri? = null

    suspend fun setCollectionDocument(collection: String, document: String, data: HashMap<String, Any>) {
        val db = Firebase.firestore.collection(collection).document(document)
        db.set(data).await()

    }

    suspend fun loadMyUser(userid: String) {
        /*val loadingScreenBinding: ActivityLoadingScreenBinding = ActivityLoadingScreenBinding.inflate(layoutInflater)
        val loadingScreenWindow = PopupWindow(loadingScreenBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        loadingScreenWindow.elevation = 5.0f

        loadingScreenWindow.showAtLocation(loadingScreenBinding.root, Gravity.CENTER, 0, 0)
        loadingScreenWindow.isFocusable = true
        loadingScreenWindow.update()*/

        val db = Firebase.firestore.collection("users").document(userid)
        val data = db.get().await().data
        if (data != null) {
            MyUser.name = data["name"] as String
            MyUser.email = data["email"] as String
            MyUser.mobile = data["mobile"] as String
            MyUser.rank = data["rank"] as String
            MyUser.score = data["score"] as Number
            profilePictureUrl = getImageUrl("profile_pictures/$userid")
        }

        //loadingScreenWindow.dismiss()
    }

    suspend fun getFieldInfoFromCollectionDocument(collection: String, document: String, field: String): Any? {
        val db = Firebase.firestore
        val ref = db.collection(collection).document(document)
        return ref.get().await().data?.get(field)
    }

    fun setFieldInCollectionDocument(collection: String, document: String, field: String, data: HashMap<String, Any?>) {
        val db = Firebase.firestore
        val ref = db.collection(collection).document(document)
        ref.set(data)
            .addOnSuccessListener {
                //TODO
            }
            .addOnFailureListener { e ->
                //TODO
            }
    }

    fun updateFieldInCollectionDocument(collection: String, document: String, field: String, data: Any?) {
        val db = Firebase.firestore
        val ref = db.collection(collection).document(document)
        ref.update(field, data)
            .addOnSuccessListener {
                //TODO
            }
            .addOnFailureListener { e ->
                //TODO
            }
    }

    fun deleteDocumentInCollection(collection: String, document: String) {
        val ref = Firebase.firestore.collection(collection).document(document)
        ref.delete()
            .addOnSuccessListener {
                //TODO
            }
            .addOnFailureListener { e ->
                //TODO
            }
    }

    fun deleteFieldInCollectionDocument(collection: String, document: String, field: String) {
        val ref = Firebase.firestore.collection(collection).document(document)

        val del = hashMapOf<String, Any?>(field to FieldValue.delete())

        ref.update(del).addOnCompleteListener { }
    }

    suspend fun getImageUrl(imagePath: String): Uri {
        val ref = storage.child(imagePath)
        return ref.downloadUrl.await()
    }

    //suspend mert csak azutan tudom elkerni az url-t, hogy a kepet feltoltotte (meg kell varni)
    suspend fun uploadImageFromImageView(imageView: ImageView, storagePath: String) {
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val data = stream.toByteArray()

        val ref = storage.child(storagePath)
        ref.putBytes(data).await()
    }

    suspend fun uploadImageFromUri(uri: Uri, storagePath: String) {
        val ref = storage.child(storagePath)
        ref.putFile(uri).await()
    }


}