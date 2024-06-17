package com.ota.noteexercise.api.notes

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Note(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    var title: String,
    var body: String
) {

}