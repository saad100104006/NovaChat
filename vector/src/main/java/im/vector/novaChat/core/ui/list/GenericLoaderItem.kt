package im.vector.novaChat.core.ui.list

import com.airbnb.epoxy.EpoxyModelClass
import im.vector.novaChat.R
import im.vector.novaChat.core.epoxy.VectorEpoxyHolder
import im.vector.novaChat.core.epoxy.VectorEpoxyModel


/**
 * A generic list item header left aligned with notice color.
 */
@EpoxyModelClass(layout = R.layout.item_generic_loader)
abstract class GenericLoaderItem : VectorEpoxyModel<GenericLoaderItem.Holder>() {

    //Maybe/Later add some style configuration, SMALL/BIG ?

    override fun bind(holder: Holder) {}

    class Holder : VectorEpoxyHolder()
}