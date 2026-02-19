package com.ko.simple_chat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ko.simple_chat.R

abstract class BaseFragment<VB : ViewBinding, T> : Fragment() {

    protected var _binding: VB? = null
    protected val binding get() = _binding!!

    // 전체 리스트
    protected val originList = mutableListOf<T>()

    // 검색 리스트
    protected val filterList = mutableListOf<T>()

    private var menuProvider: MenuProvider? = null


    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        menuProvider?.let {
            requireActivity().removeMenuProvider(it)
        }

        menuProvider = null
        _binding = null
    }

    /* ===========================+
        Toolbar Setting
     =============================*/

    protected fun setToolbar(
        showToolbar: Boolean,
        title: String,
        showBack: Boolean,
    ) {
        (requireActivity() as AppCompatActivity).apply {
            if (showToolbar) supportActionBar?.show() else supportActionBar?.hide()
            supportActionBar?.title = title
            supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
        }
    }

    /* ===========================+
        Toolbar search
     =============================*/


    // 검색 조건(각 Fragment에서 구현)
    protected abstract fun match(item: T, keyword: String): Boolean

    // Adapter 반영(각 Fragment에서 구현)
    protected abstract fun submitList(list: List<T>)

    // 검색 필터
    protected fun filter(text: String) {
        filterList.clear()

        if (text.isBlank()) {
            filterList.addAll(originList)
        } else {
            val keyword = text.lowercase()

            originList.forEach { item ->
                if (match(item, keyword)) {
                    filterList.add(item)
                }
            }
        }
        submitList(filterList)
    }

    // SearchView 세팅
    protected fun initSearchView(searchView: SearchView) {

        searchView.queryHint = "검색어 입력"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(text: String?): Boolean {
                filter(text.orEmpty())
                return true
            }

            override fun onQueryTextSubmit(text: String?): Boolean {
                filter(text.orEmpty())
                return true
            }
        })
    }

    /* ===========================+
        Menu Setting
    =============================*/

    protected fun setUpMenu() {

        if (menuProvider != null) return

        menuProvider = object : MenuProvider {
            override fun onCreateMenu(
                menu: Menu,
                menuInflater: MenuInflater
            ) {

                menuInflater.inflate(R.menu.menu_toolbar, menu)

                val searchItem = menu.findItem(R.id.toolbar_search)
                val searchView = searchItem.actionView as? SearchView ?: return

                initSearchView(searchView)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().popBackStack()
                        true
                    }

                    else -> false
                }
            }
        }

        requireActivity().addMenuProvider(
            menuProvider!!,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }
}