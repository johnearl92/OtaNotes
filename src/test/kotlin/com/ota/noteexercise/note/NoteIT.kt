package com.ota.noteexercise.note

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ota.noteexercise.api.notes.Note
import com.ota.noteexercise.api.notes.NoteRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class NoteIT : BehaviorSpec() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var noteRepository: NoteRepository

    val mapper = jacksonObjectMapper()

    init {
        beforeSpec {
            noteRepository.deleteAll()
        }

        Given("a note with title 'Monday' and body 'day one, still alive'") {
            val name = "Monday"
            val body = "day one, still alive"
            var note = Note(title = name, body = body)

            When("creating a new note") {
                note = mockMvc.post("/api/v1/note") {
                    contentType = MediaType.APPLICATION_JSON
                    content = mapper.writeValueAsString(note)
                    with(SecurityMockMvcRequestPostProcessors.csrf())
                }.andExpect {
                    status { isCreated() }
                }.andReturn().response.contentAsString.let {
                    mapper.readValue(it, Note::class.java)
                }
                Then("note is created with new id") {
                    note.id shouldNotBe null
                    note.title shouldBe name
                    note.body shouldBe body
                }
            }

            When("getting all notes") {
                val allNotes = mockMvc.get("/api/v1/note") {
                    contentType = MediaType.APPLICATION_JSON
                    with(SecurityMockMvcRequestPostProcessors.csrf())
                }.andExpect {
                    status { isOk() }
                }.andReturn().response.contentAsString.let {
                    mapper.readValue(it, Array<Note>::class.java)
                }

                Then("the note should be in the list") {
                    allNotes shouldContain note
                }
            }

            When("getting the note by id") {
                and("the id is valid") {
                    val noteById = mockMvc.get("/api/v1/note/${note.id}") {
                        contentType = MediaType.APPLICATION_JSON
                        with(SecurityMockMvcRequestPostProcessors.csrf())
                    }.andExpect {
                        status { isOk() }
                    }.andReturn().response.contentAsString.let {
                        mapper.readValue(it, Note::class.java)
                    }

                    Then("the note shoulld be returned") {
                        noteById shouldBe note
                    }
                }

                and("the id is not valid") {
                    val request = mockMvc.get("/api/v1/note/2") {
                        with(SecurityMockMvcRequestPostProcessors.csrf())
                    }

                    Then("no note should be returned") {
                        request.andExpect { status { isNotFound() } }
                    }
                }
            }

            When("updating the note") {
                val updatedNote = note.copy(body = "I'm dead!")
                and("the note id is valid") {
                    val updatedNoteFromDb = mockMvc.put("/api/v1/note/${note.id}") {
                        contentType = MediaType.APPLICATION_JSON
                        content = mapper.writeValueAsString(updatedNote)
                        with(SecurityMockMvcRequestPostProcessors.csrf())
                    }.andExpect {
                        status { isOk() }
                    }.andReturn().response.contentAsString.let {
                        mapper.readValue(it, Note::class.java)
                    }

                    Then("the note should be updated") {
                        updatedNoteFromDb shouldBe updatedNote
                    }
                }
                and("the note id is invalid") {
                    val result = mockMvc.put("/api/v1/note/2") {
                        contentType = MediaType.APPLICATION_JSON
                        content = mapper.writeValueAsString(updatedNote)
                        with(SecurityMockMvcRequestPostProcessors.csrf())
                    }

                    Then("no note should be updated") {
                        result.andExpect { status { isNotFound() } }
                    }
                }

            }

            When("deleting a note") {
                and("the note id is valid") {
                    mockMvc.delete("/api/v1/note/${note.id}") {
                        with(SecurityMockMvcRequestPostProcessors.csrf())
                    }.andExpect { status { isNoContent() } }
                    Then("note should be deleted") {
                        mockMvc.get("/api/v1/note/${note.id}") {
                            with(SecurityMockMvcRequestPostProcessors.csrf())
                            with(
                                SecurityMockMvcRequestPostProcessors.user("tester")
                                    .authorities(SimpleGrantedAuthority("SCOPE_ota.read"))
                            )
                        }.andExpect { status { isNotFound() } }
                    }
                }

                and("the note id is invalid") {
                    val resultAction = mockMvc.delete("/api/v1/note/2") {
                        with(SecurityMockMvcRequestPostProcessors.csrf())
                    }
                    Then("no note should be deleted") {
                        resultAction.andExpect { status { isNotFound() } }
                    }
                }
            }
        }
    }
}