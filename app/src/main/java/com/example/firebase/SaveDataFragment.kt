package com.example.firebase

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.firebase.base.BaseFragment
import com.example.firebase.databinding.FragmentSaveDataBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.lang.StringBuilder


class SaveDataFragment : BaseFragment<FragmentSaveDataBinding>(){
private val collection = Firebase.firestore.collection("person")
    override fun getBinding() = R.layout.fragment_save_data
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
binding.buttonSave.setOnClickListener {
    val person = oldPerson()
    saveDataPerson(person)
}
        binding.buttonRead.setOnClickListener {
            readData()
        }
        binding.buttonUpdate.setOnClickListener {
            val oldPerson = oldPerson()
            val newPerson = newPerson()
            upDatePerson(oldPerson, newPerson)
        }
        binding.buttomDeletePerson.setOnClickListener {
            val person = oldPerson()
            deletePerson(person)
        }
        binding.buttonBatchedWrite.setOnClickListener {
            chagedName("oBMFKXKxRBQ5x6buXktL", "Vasia", "Kotovski")
        }
        binding.buttonTransaction.setOnClickListener {
            birthday("oBMFKXKxRBQ5x6buXktL")
        }
    }
    private fun newPerson() :Map<String, Any> {
        val firstName = binding.editFirstNameNew.text.toString()
        val lastName = binding.editNewLastName.text.toString()
        val age = binding.editNewAge.text.toString()
        val map = mutableMapOf<String , Any>()
        if(firstName.isNotEmpty()){
            map["firstName"] = firstName
        }
        if (lastName.isNotEmpty()){
            map["lastName"] = lastName
        }
        if(age.isNotEmpty()){
            map["age"] = age
        }
        return map
    }

    private fun birthday(personID: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            Firebase.firestore.runTransaction{transaction->
            val personRef = collection.document(personID)
                val person = transaction.get(personRef)
                val newAge = person["age"] as Long + 1
                transaction.update(personRef, "age", newAge)
                null
            }.await()
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                snackBar("${e}")
            }
        }
    }

    private fun chagedName(personId: String, newFirstName: String, newLastName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
Firebase.firestore.runBatch { batch->
val personRef = collection.document(personId)
    batch.update(personRef, "firstName", newFirstName)
    batch.update(personRef, "lastName", newLastName)

}.await()
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                snackBar("${e}")
            }
        }
    }

    private fun upDatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = collection.
        whereEqualTo("lastName", person.lastName)
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()){
            for(document in personQuery){
                try {
                    collection.document(document.id).set(
                        newPersonMap,
                        SetOptions.merge()
                    ).await()
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        snackBar("${e}")
                    }
                }
            }
        } else{
            withContext(Dispatchers.Main){
                snackBar("not person method query")
            }
        }
    }


    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = collection.
        whereEqualTo("lastName", person.lastName)
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if(personQuery.documents.isNotEmpty()){
            for(document in personQuery){
                try {
                   // collection.document(document.id).delete().await() // удаляем все значения
                    collection.document(document.id).update(mapOf( // удалим только поле со значением firstName
                        "firstName" to FieldValue.delete()
                    )
                    )
                }catch (e: Exception){
                    withContext(Dispatchers.Main){
                        snackBar("${e}")
                    }
                }
            }
        } else{
            withContext(Dispatchers.Main){
                snackBar("not person method query")
            }
        }
    }

    private fun oldPerson(): Person {
        val firstName = binding.editFirstName.text.toString()
        val lastName = binding.editLastName.text.toString()
        val age = binding.editAge.text.toString()
       return Person(firstName, lastName, age)
    }

    private fun subScribeToRealtimeUpdates() {
        collection.
        //whereEqualTo("firstName", "Ilya"). // в этой функции мы можем указать какое поле мы будем обновлять
        addSnapshotListener{querySnapshot, firebaseFirstoreException ->
            firebaseFirstoreException?.let {
                it.message?.let { it1 -> snackBar(it1) }
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val sb = StringBuilder()
                for(document in it){
                    val person = document.toObject<Person>()
                    sb.append("${person}\n")
                }
                binding.textReadData.text = sb.toString()
            }
        }
    }

    private fun readData() = CoroutineScope(Dispatchers.IO).launch {
        val fromAge = binding.edit.text.toString()
        val toAge = binding.edit2.text.toString()
        try {
            val querySnapshot = collection.
                    whereGreaterThan("age", fromAge)
                .whereLessThan("age", toAge)
                .orderBy("age")
                //.whereEqualTo("firstName", "Piter") // если нам надо искать по имени firstName это поле где мы будем искать а , второе это значение Имя
                .get().await()
            val sb = StringBuilder()
            for(document in querySnapshot.documents){
                val person = document.toObject<Person>()
                sb.append("${person}\n")
            }
            withContext(Dispatchers.Main){
                binding.personData.text = sb.toString()
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                snackBar("${e}")
            }
        }
    }

    private fun saveDataPerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            collection.add(person).await()
            withContext(Dispatchers.Main){
                snackBar("succseful")
            }
        }catch (e: Exception){
            withContext(Dispatchers.Main){
                snackBar("${e}")
            }
        }
    }
}