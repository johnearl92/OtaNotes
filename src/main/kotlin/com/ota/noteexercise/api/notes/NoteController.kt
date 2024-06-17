package com.ota.noteexercise.api.notes

import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/v1/note")
class NoteController(private val noteRepository: NoteRepository) {
    private val logger = KotlinLogging.logger {}

    @GetMapping
    fun getAllNotes(): List<Note> {
        logger.info { "Fetching all notes" }
        return noteRepository.findAll()
    }

    @GetMapping("/{id}")
    fun getNote(@PathVariable id: Long): Note {
        logger.info { "Fetching note with id $id" }
        return noteRepository.findById(id).orElseThrow {
            val errorCode = HttpStatus.NOT_FOUND
            MDC.put("RESPONSE_CODE", errorCode.value().toString())
            logger.error { "Note with id $id not found" }
            ResponseStatusException(errorCode, "Note not found")
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNote(@RequestBody note: Note): Note {
        logger.info { "Creating new note: $note" }
        return noteRepository.save(note)
    }

    @PutMapping("/{id}")
    fun updateNote(@PathVariable id: Long, @RequestBody note: Note): Note {
        logger.info { "Updating note with id $id" }
        return noteRepository.findById(id).map {
            noteRepository.save(note.copy(id = id))
        }.orElseThrow {
            val errorCode = HttpStatus.NOT_FOUND
            MDC.put("RESPONSE_CODE", errorCode.value().toString())
            logger.error { "Note with id $id not found" }
            ResponseStatusException(errorCode, "Note not found")
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteNote(@PathVariable id: Long) {
        logger.info { "Deleting note with id $id" }
        noteRepository.findById(id).map {
            noteRepository.deleteById(id)
        }.orElseThrow {
            val errorCode = HttpStatus.NOT_FOUND
            MDC.put("RESPONSE_CODE", errorCode.value().toString())
            logger.error { "Note with id $id not found" }
            ResponseStatusException(errorCode, "Note not found")
        }
    }
}