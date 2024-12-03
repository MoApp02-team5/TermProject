package com.example.termproject.ui.register

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(): ViewModel(){
    private val _state = MutableStateFlow<RegisterState>(RegisterState.Nothing)
    val state = _state.asStateFlow()

    fun signUp(name: String, email: String, password: String){
        _state.value = RegisterState.Loading
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    task.result.user?.let {
                        it.updateProfile(
                            com.google.firebase.auth.UserProfileChangeRequest
                                .Builder()
                                .setDisplayName(name)
                                .build()
                        ).addOnCompleteListener{
                            _state.value = RegisterState.Success
                        }
                    }

                }else {
                    _state.value = RegisterState.Error
                }
            }
    }
}

sealed class RegisterState {
    object Nothing: RegisterState()
    object Loading: RegisterState()
    object Success: RegisterState()
    object Error: RegisterState()
}