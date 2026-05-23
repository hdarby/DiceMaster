package com.hdarby.dicemaster.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hdarby.dicemaster.domain.model.ConsumableItem
import com.hdarby.dicemaster.domain.usecase.item.AddItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.AssignItemToCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.DeleteItemUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsByCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.GetItemsUseCase
import com.hdarby.dicemaster.domain.usecase.item.UnassignItemFromCharacterUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemQuantityUseCase
import com.hdarby.dicemaster.domain.usecase.item.UpdateItemUseCase
import com.hdarby.dicemaster.viewmodel.state.ItemUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_ITEM_QUANTITY = 1

class ItemViewModel(
    private val getItemsUseCase: GetItemsUseCase,
    private val getItemsByCharacterUseCase: GetItemsByCharacterUseCase,
    private val addItemUseCase: AddItemUseCase,
    private val updateItemUseCase: UpdateItemUseCase,
    private val deleteItemUseCase: DeleteItemUseCase,
    private val assignItemToCharacterUseCase: AssignItemToCharacterUseCase,
    private val unassignItemFromCharacterUseCase: UnassignItemFromCharacterUseCase,
    private val updateItemQuantityUseCase: UpdateItemQuantityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemUiState())
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    init {
        loadItems()
        loadItemsByCharacter()
    }

    private fun loadItems() {
        getItemsUseCase()
            .onStart { _uiState.update { it.copy(isLoading = true) } }
            .onEach { items -> _uiState.update { it.copy(items = items, isLoading = false) } }
            .catch { error -> _uiState.update { it.copy(error = error.message, isLoading = false) } }
            .launchIn(viewModelScope)
    }

    private fun loadItemsByCharacter() {
        getItemsByCharacterUseCase()
            .onEach { map -> _uiState.update { it.copy(itemsByCharacterId = map) } }
            .catch { error -> _uiState.update { it.copy(error = error.message) } }
            .launchIn(viewModelScope)
    }

    fun addItem(item: ConsumableItem) {
        viewModelScope.launch {
            try {
                addItemUseCase(item)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateItem(item: ConsumableItem) {
        viewModelScope.launch {
            try {
                updateItemUseCase(item)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteItem(item: ConsumableItem) {
        viewModelScope.launch {
            try {
                deleteItemUseCase(item)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun assignItem(characterId: Long, itemId: Long) {
        viewModelScope.launch {
            try {
                val quantity = _uiState.value.items.find { it.id == itemId }?.totalQuantity ?: MIN_ITEM_QUANTITY
                assignItemToCharacterUseCase(characterId, itemId, quantity)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun unassignItem(characterId: Long, itemId: Long) {
        viewModelScope.launch {
            try {
                unassignItemFromCharacterUseCase(characterId, itemId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun incrementQuantity(characterId: Long, itemId: Long, currentQuantity: Int) {
        viewModelScope.launch {
            try {
                updateItemQuantityUseCase(characterId, itemId, currentQuantity + 1)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun decrementQuantity(characterId: Long, itemId: Long, currentQuantity: Int) {
        viewModelScope.launch {
            try {
                if (currentQuantity <= MIN_ITEM_QUANTITY) {
                    unassignItemFromCharacterUseCase(characterId, itemId)
                } else {
                    updateItemQuantityUseCase(characterId, itemId, currentQuantity - 1)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
