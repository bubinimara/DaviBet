package io.github.bubinimara.davibet.ui

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val autoScrollListener = AutoScrollListener()

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
        viewBinding.recyclerView.addOnScrollListener(autoScrollListener)


        viewModel.tweets.observe(viewLifecycleOwner, Observer {
            adapter.set(it)
            if(autoScrollListener.shouldAutoScroll)
                viewBinding.recyclerView.scrollToPosition(0)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewBinding = null
    }

    class AutoScrollListener: RecyclerView.OnScrollListener() {
        var shouldAutoScroll = true

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                Log.d("MainFragment", "onScrollStateChanged: false")
                // user is scrolling
                shouldAutoScroll = false
            }else{
                Log.d("MainFragment", "onScrollStateChanged: can scroll "+!recyclerView.canScrollVertically(-1))
                shouldAutoScroll = !recyclerView.canScrollVertically(-1)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
        }

    }
}