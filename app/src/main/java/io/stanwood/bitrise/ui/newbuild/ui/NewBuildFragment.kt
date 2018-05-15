package io.stanwood.bitrise.ui.newbuild.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.stanwood.bitrise.R
import io.stanwood.bitrise.databinding.FragmentNewBuildBinding
import io.stanwood.bitrise.navigation.SCREEN_BUILDS
import io.stanwood.bitrise.ui.newbuild.vm.NewBuildViewModel
import org.koin.android.ext.android.inject
import ru.terrakok.cicerone.Router

class NewBuildFragment : Fragment() {
    companion object {
        fun newInstance() = NewBuildFragment()
    }

    private val viewModel: NewBuildViewModel by inject()

    private val router: Router by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentNewBuildBinding.inflate(inflater, container, false).apply {
            vm = viewModel
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Toolbar>(R.id.toolbar)?.let {
            it.navigationIcon = ContextCompat.getDrawable(it.context, R.drawable.ic_arrow_back)
            it.setNavigationOnClickListener {
                router.backTo(SCREEN_BUILDS)
            }
        }
    }
}