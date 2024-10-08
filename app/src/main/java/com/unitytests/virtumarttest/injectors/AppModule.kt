package com.unitytests.virtumarttest.injectors

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.unitytests.virtumarttest.firebase.CartHandleFirebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuthentication()=FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestoreDB()=Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseCommonClass(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ) = CartHandleFirebase(firestore, firebaseAuth)

    @Provides
    @Singleton
    fun provideStorage() = FirebaseStorage.getInstance().reference
}