package com.example.myapplication2.ui

interface OnStepCompletionListener {
    fun onStepCompleted(stepName: String, isCompleted: Boolean)
}