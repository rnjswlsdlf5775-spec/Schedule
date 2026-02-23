package com.schedule.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.schedule.app.databinding.FragmentSettingsBinding
import com.schedule.app.util.ThemeManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupThemeSelection()
        loadCurrentSettings()
    }

    private fun setupThemeSelection() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val themeMode = when (checkedId) {
                binding.rbLight.id -> ThemeManager.THEME_LIGHT
                binding.rbDark.id -> ThemeManager.THEME_DARK
                else -> ThemeManager.THEME_SYSTEM
            }
            lifecycleScope.launch {
                ThemeManager.setTheme(requireContext(), themeMode)
            }
        }
    }

    private fun loadCurrentSettings() {
        lifecycleScope.launch {
            ThemeManager.getThemeFlow(requireContext()).collect { themeMode ->
                val checkedId = when (themeMode) {
                    ThemeManager.THEME_LIGHT -> binding.rbLight.id
                    ThemeManager.THEME_DARK -> binding.rbDark.id
                    else -> binding.rbSystem.id
                }
                binding.rgTheme.check(checkedId)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
