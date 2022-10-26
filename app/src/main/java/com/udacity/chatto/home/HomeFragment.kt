package com.udacity.chatto.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.udacity.chatto.MainViewmodel
import com.udacity.chatto.R
import com.udacity.chatto.databinding.FragmentHomeBinding
import com.udacity.chatto.databinding.FragmentSigninBinding

class HomeFragment : Fragment() {

    private lateinit var _binding: FragmentHomeBinding

    private lateinit var viewModel: HomeViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        _binding.lifecycleOwner=this
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.uid.observe(viewLifecycleOwner){
            if(it.isNullOrEmpty()){
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSigninFragment())
                val mainviewmodel=ViewModelProvider(requireActivity()).get(MainViewmodel::class.java)
                mainviewmodel.uid.value=""
            }
        }
        setHasOptionsMenu(true)
        return _binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.action_logout)
        {
            Firebase.auth.signOut()

        }
        return super.onOptionsItemSelected(item)
    }
}