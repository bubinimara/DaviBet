package io.github.bubinimara.davibet.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.bubinimara.davibet.R
import io.github.bubinimara.davibet.databinding.MainFragmentBinding
import io.github.bubinimara.davibet.ui.adapter.TweetAdapter

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private var _viewBinding: MainFragmentBinding? = null
    private val viewBinding get() = _viewBinding!!
    private val adapter = TweetAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = MainFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.load()


        viewBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.adapter = adapter

        viewModel.tweets.observe(viewLifecycleOwner, Observer {
            adapter.set(it)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }
}