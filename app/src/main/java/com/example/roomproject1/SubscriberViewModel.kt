package com.example.roomproject1

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomproject1.db.Subscriber
import com.example.roomproject1.db.SubscriberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriberViewModel(private val repository: SubscriberRepository) : ViewModel() {

    val subscribers = repository.subscribers
    private var isUpdateOrDelete = false
    private lateinit var subscriberToUpdateOrDelete : Subscriber

    val inputName = MutableLiveData<String>()
    val inputEmail = MutableLiveData<String>()

    val saveOrUpdateButtonText = MutableLiveData<String>()
    val clearAllOrDeleteButtonText = MutableLiveData<String>()

    private val statusMessage = MutableLiveData<Event<String>>()

    val message : LiveData<Event<String>>
        get() = statusMessage

    init {
        saveOrUpdateButtonText.value = "Save"
        clearAllOrDeleteButtonText.value = "Clear All"
    }

    fun saveOrUpdate() {
        when {
            inputName.value.isNullOrEmpty() ->
                statusMessage.value = Event("Please enter subscriber's name")

            inputEmail.value.isNullOrEmpty() ->
                statusMessage.value = Event("Please enter subscriber's email")

            !Patterns.EMAIL_ADDRESS.matcher(inputEmail.value!!).matches() ->
                statusMessage.value = Event("Please enter a correct email address")

            else -> {
                if (isUpdateOrDelete) {
                    subscriberToUpdateOrDelete.name = inputName.value ?: ""
                    subscriberToUpdateOrDelete.email = inputEmail.value ?: ""
                    update(subscriberToUpdateOrDelete)
                } else {
                    val name = inputName.value ?: ""
                    val email = inputEmail.value ?: ""
                    insert(Subscriber(name, email, 0))
                    inputName.value = ""
                    inputEmail.value = ""
                }
            }
        }
    }

    fun clearAllOrDelete() {
        if (isUpdateOrDelete) delete(subscriberToUpdateOrDelete) else clearAll()
    }

    private fun insert(subscriber: Subscriber) = viewModelScope.launch(Dispatchers.IO) {
        val newRowId = repository.insert(subscriber)
        withContext(Dispatchers.Main){
            statusMessage.value = if (newRowId > 1) {
                Event("Subscriber Inserted Successfully with Id : $newRowId")
            } else {
                Event("Error Occurred!")
            }
        }
    }

    private fun update(subscriber: Subscriber) = viewModelScope.launch(Dispatchers.IO) {
        val numRowsUpdated = repository.update(subscriber)
        withContext(Dispatchers.Main) {
            if (numRowsUpdated > 0) {
                inputName.value = ""
                inputEmail.value = ""
                isUpdateOrDelete = false
                saveOrUpdateButtonText.value = "Save"
                clearAllOrDeleteButtonText.value = "Clear All"
                statusMessage.value = Event("$numRowsUpdated rows Updated Successfully!")
            } else {
                statusMessage.value = Event("Error Occurred!")
            }
        }
    }

    private fun delete(subscriber: Subscriber) = viewModelScope.launch(Dispatchers.IO) {
        val numRowsDeleted = repository.delete(subscriber)
        withContext(Dispatchers.Main){
            if (numRowsDeleted > 0){
                inputName.value = ""
                inputEmail.value = ""
                isUpdateOrDelete = false
                saveOrUpdateButtonText.value = "Save"
                clearAllOrDeleteButtonText.value = "Clear All"
                statusMessage.value = Event("$numRowsDeleted rows Deleted Successfully!")
            } else{
                statusMessage.value = Event("Error Occurred")
            }
        }
    }

    private fun clearAll() = viewModelScope.launch(Dispatchers.IO) {
        val numRowsDeleted = repository.deleteAll()
        withContext(Dispatchers.Main) {
            statusMessage.value = if (numRowsDeleted > 0) {
                Event("$numRowsDeleted rows Deleted Successfully!")
            } else {
                Event("Error Occurred!")
            }
        }
    }

    fun initUpdateAndDelete(subscriber : Subscriber){
        inputName.value = subscriber.name
        inputEmail.value = subscriber.email
        isUpdateOrDelete = true
        subscriberToUpdateOrDelete = subscriber
        saveOrUpdateButtonText.value = "Update"
        clearAllOrDeleteButtonText.value = "Delete"
    }
}
