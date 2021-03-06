package com.yogpc.qp.machines.generator.creative;

import java.util.Arrays;
import java.util.Objects;

import com.yogpc.qp.machines.base.APacketTile;
import com.yogpc.qp.machines.base.APowerTile;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;
import scala.Symbol;

import static jp.t2v.lab.syntax.MapStreamSyntax.byValue;
import static jp.t2v.lab.syntax.MapStreamSyntax.toAny;

public class CreativeGeneratorTile extends APacketTile implements ITickableTileEntity {
    public static final Symbol SYMBOL = Symbol.apply("CreativeGenerator");
    public long sendAmount = 10000L * APowerTile.FEtoMicroJ;
    private final LazyOptional<IEnergyStorage> handlerOptional = LazyOptional.of(Handler::new);

    public CreativeGeneratorTile() {
        super(Holder.creativeGeneratorType());
    }

    @Override
    public void tick() {
        if (world != null && !world.isRemote) {
            Arrays.stream(Direction.values())
                .map(d -> Pair.of(d, world.getTileEntity(pos.offset(d))))
                .filter(byValue(Objects::nonNull))
                .map(toAny((direction, t) -> t.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite())))
                .forEach(l -> l.ifPresent(e -> {
                    if (e instanceof APowerTile) {
                        ((APowerTile) e).getEnergy(sendAmount, true);
                    } else {
                        int accepted = e.receiveEnergy(energyInFE(), true);
                        if (accepted > 0) {
                            e.receiveEnergy(accepted, false);
                        }
                    }
                }));
        }
    }

    private int energyInFE() {
        return (int) (sendAmount / APowerTile.FEtoMicroJ);
    }

    @Override
    public void func_230337_a_(BlockState state, CompoundNBT nbt) {
        sendAmount = nbt.getLong("sendAmount");
        super.func_230337_a_(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putLong("sendAmount", sendAmount);
        return super.write(compound);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) return handlerOptional.cast();
        return super.getCapability(cap, side);
    }

    class Handler implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return Math.min(maxExtract, getEnergyStored());
        }

        @Override
        public int getEnergyStored() {
            return energyInFE();
        }

        @Override
        public int getMaxEnergyStored() {
            return energyInFE();
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
