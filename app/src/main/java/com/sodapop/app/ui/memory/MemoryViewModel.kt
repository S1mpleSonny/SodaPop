package com.sodapop.app.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sodapop.app.data.repository.ClusterRepository
import com.sodapop.app.data.repository.LlmRepository
import com.sodapop.app.data.repository.ThoughtRepository
import com.sodapop.app.domain.model.MemoryLayer
import com.sodapop.app.domain.model.Thought
import com.sodapop.app.domain.model.TopicCluster
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val thoughtRepository: ThoughtRepository,
    private val clusterRepository: ClusterRepository,
    private val llmRepository: LlmRepository
) : ViewModel() {

    val fragments: StateFlow<List<Thought>> = thoughtRepository.getByLayer(MemoryLayer.FRAGMENT)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val topics: StateFlow<List<Thought>> = thoughtRepository.getByLayer(MemoryLayer.TOPIC)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val beliefs: StateFlow<List<Thought>> = thoughtRepository.getByLayer(MemoryLayer.BELIEF)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val clusters: StateFlow<List<TopicCluster>> = clusterRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun promoteThought(thought: Thought) {
        viewModelScope.launch {
            val newLayer = when (thought.layer) {
                MemoryLayer.FRAGMENT -> MemoryLayer.TOPIC
                MemoryLayer.TOPIC -> MemoryLayer.BELIEF
                MemoryLayer.BELIEF -> return@launch
            }
            val updated = thought.copy(
                layer = newLayer,
                updatedAt = System.currentTimeMillis()
            )
            thoughtRepository.update(updated)
        }
    }

    fun deleteThought(thought: Thought) {
        viewModelScope.launch {
            thoughtRepository.delete(thought)
        }
    }
}
