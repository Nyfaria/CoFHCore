package cofh.lib.inventory.wrapper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.List;

public class InvWrapperGeneric implements IInventory {

    private NonNullList<ItemStack> stackList;

    private final Container eventHandler;

    public InvWrapperGeneric(Container eventHandler, List<ItemStack> contents, int size) {

        this.stackList = NonNullList.withSize(size, ItemStack.EMPTY);
        this.eventHandler = eventHandler;

        readFromContainerInv(contents);
    }

    public List<ItemStack> getStacks() {

        return stackList;
    }

    public void readFromContainerInv(List<ItemStack> contents) {

        this.stackList.clear();
        for (int i = 0; i < Math.min(contents.size(), getContainerSize()); ++i) {
            this.stackList.set(i, contents.get(i));
        }
    }

    // region IInventory
    @Override
    public int getContainerSize() {

        return this.stackList.size();
    }

    public boolean isEmpty() {

        for (ItemStack stack : this.stackList) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {

        return index >= this.getContainerSize() ? ItemStack.EMPTY : this.stackList.get(index);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {

        return ItemStackHelper.takeItem(this.stackList, index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {

        return ItemStackHelper.removeItem(this.stackList, index, count);
    }

    @Override
    public void setItem(int index, ItemStack stack) {

        if (index >= 0 && index < getContainerSize()) {
            this.stackList.set(index, stack);
            this.eventHandler.slotsChanged(this);
        }
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(PlayerEntity player) {

        return true;
    }

    @Override
    public void clearContent() {

        this.stackList.clear();
    }
    // endregion
}
