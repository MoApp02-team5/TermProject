package com.example.termproject.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(): ViewModel(){
    private val _state = MutableStateFlow<LoginState>(LoginState.Nothing)
    val state = _state.asStateFlow()

    fun signIn(email: String, password: String){
        _state.value = LoginState.Loading
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if(task.isSuccessful){
                    _state.value = LoginState.Success
                }else {
                    _state.value = LoginState.Error
                }
            }
    }
}

sealed class LoginState {
    object Nothing: LoginState()
    object Loading: LoginState()
    object Success: LoginState()
    object Error: LoginState()
}