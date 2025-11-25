package com.example.sprintproject.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sprintproject.model.User;
import com.example.sprintproject.model.UserRepository;

public class AuthViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        userRepository = new UserRepository();
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void createUser(String email, String password) {
        userRepository.createUser(email, new UserRepository.CreateUserCallback() {
            @Override
            public void onSuccess(User user) {
                userLiveData.postValue(user);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue(error);
            }
        });
    }
}