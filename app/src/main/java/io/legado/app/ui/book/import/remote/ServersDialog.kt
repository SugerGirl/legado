package io.legado.app.ui.book.import.remote

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.RecyclerAdapter
import io.legado.app.data.appDb
import io.legado.app.data.entities.Server
import io.legado.app.databinding.DialogRecyclerViewBinding
import io.legado.app.databinding.ItemServerSelectBinding
import io.legado.app.lib.theme.backgroundColor
import io.legado.app.lib.theme.primaryColor
import io.legado.app.ui.widget.recycler.VerticalDivider
import io.legado.app.utils.applyTint
import io.legado.app.utils.setLayout
import io.legado.app.utils.showDialogFragment
import io.legado.app.utils.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch

class ServersDialog : BaseDialogFragment(R.layout.dialog_recycler_view),
    Toolbar.OnMenuItemClickListener {

    val binding by viewBinding(DialogRecyclerViewBinding::bind)
    val viewModel by viewModels<ServersViewModel>()

    private val callback get() = (activity as? Callback)
    private val adapter by lazy { ServersAdapter(requireContext()) }

    override fun onStart() {
        super.onStart()
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }


    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolBar.setBackgroundColor(primaryColor)
        binding.toolBar.setTitle(R.string.server_config)
        initView()
        initData()
    }

    private fun initView() {
        binding.toolBar.inflateMenu(R.menu.servers)
        binding.toolBar.menu.applyTint(requireContext())
        binding.toolBar.setOnMenuItemClickListener(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.addItemDecoration(VerticalDivider(requireContext()))
        binding.recyclerView.adapter = adapter
    }

    private fun initData() {
        launch {
            appDb.serverDao.observeAll().collect{
                adapter.setItems(it)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add -> showDialogFragment(ServerConfigDialog())
        }
        return true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback?.onDialogDismiss("serversDialog")
    }

    inner class ServersAdapter(context: Context) :
        RecyclerAdapter<Server, ItemServerSelectBinding>(context) {

        private var selectServerId: Long? = null

        override fun getViewBinding(parent: ViewGroup): ItemServerSelectBinding {
            return ItemServerSelectBinding.inflate(inflater, parent, false)
        }

        override fun registerListener(holder: ItemViewHolder, binding: ItemServerSelectBinding) {
            binding.rbServer.setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed && isChecked) {
                    selectServerId = getItemByLayoutPosition(holder.layoutPosition)?.id
                }
            }
            binding.tvEdit.setOnClickListener {
                getItemByLayoutPosition(holder.layoutPosition)?.let { server ->
                    showDialogFragment(ServerConfigDialog(server.id))
                }
            }
        }

        override fun convert(
            holder: ItemViewHolder,
            binding: ItemServerSelectBinding,
            item: Server,
            payloads: MutableList<Any>
        ) {
            if (payloads.isEmpty()) {
                binding.root.setBackgroundColor(context.backgroundColor)
                binding.rbServer.text = item.name
                binding.rbServer.isChecked = item.id == selectServerId
            } else {
                binding.rbServer.isChecked = item.id == selectServerId
            }
        }

    }

    interface Callback {

        fun onDialogDismiss(tag: String)

    }

}