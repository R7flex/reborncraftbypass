package com.r7flex.reborncraft.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.client.network.ClientPlayerInteractionManager;

@Mixin(MinecraftClient.class)
public class ExampleClientMixin {
	private boolean isVerificationMenuOpen = false;
	private HandledScreen<?> pendingVerificationScreen = null;

	@Inject(at = @At("HEAD"), method = "setScreen")
	private void onScreenOpen(Screen screen, CallbackInfo info) {
		System.out.println("[REBORNCRAFTBYPASS] setScreen tetiklendi: " + (screen != null ? screen.getClass().getName() : "null"));
		if (screen instanceof HandledScreen) {
			HandledScreen<?> handledScreen = (HandledScreen<?>) screen;
			Text title = handledScreen.getTitle();
			System.out.println("[REBORNCRAFTBYPASS] Screen title: " + title.getString());
			if (title.getString().contains("Doğrulama")) {
				System.out.println("[REBORNCRAFTBYPASS] Doğrulama Menüsü algılandı!");
				isVerificationMenuOpen = true;
				pendingVerificationScreen = handledScreen;
			} else {
				isVerificationMenuOpen = false;
				pendingVerificationScreen = null;
			}
		}
	}

	@Inject(at = @At("TAIL"), method = "tick")
	private void onClientTick(CallbackInfo info) {
		if (isVerificationMenuOpen && pendingVerificationScreen != null) {
			boolean clicked = handleVerificationMenu(pendingVerificationScreen);
			if (clicked) {
				isVerificationMenuOpen = false;
				pendingVerificationScreen = null;
			}
		}
	}

	private boolean handleVerificationMenu(HandledScreen<?> screen) {
		if (screen instanceof GenericContainerScreen) {
			GenericContainerScreen containerScreen = (GenericContainerScreen) screen;
			List<Slot> slots = containerScreen.getScreenHandler().slots;
			int chestSlotCount = 0;
			if (slots.size() >= 54) chestSlotCount = 54;
			else if (slots.size() >= 27) chestSlotCount = 27;
			else chestSlotCount = slots.size();
			System.out.println("[REBORNCRAFTBYPASS] Chest slot count: " + chestSlotCount);
			for (int i = 0; i < chestSlotCount; i++) {
				ItemStack stack = slots.get(i).getStack();
				System.out.println("[REBORNCRAFTBYPASS] Slot " + i + ": " + (stack.isEmpty() ? "BOŞ" : stack.getItem().toString()));
			}
			ItemStack referenceItem = ItemStack.EMPTY;
			int referenceIndex = -1;
			// İlk dolu itemi bul (sadece chest kısmı)
			for (int i = 0; i < chestSlotCount; i++) {
				ItemStack stack = slots.get(i).getStack();
				if (!stack.isEmpty()) {
					referenceItem = stack;
					referenceIndex = i;
					System.out.println("[REBORNCRAFTBYPASS] Referans item: slot=" + i + ", item=" + stack.getItem().toString());
					break;
				}
			}
			if (!referenceItem.isEmpty()) {
				for (int i = 0; i < chestSlotCount; i++) {
					if (i == referenceIndex) continue;
					ItemStack currentItem = slots.get(i).getStack();
					if (!currentItem.isEmpty() && !currentItem.getItem().equals(referenceItem.getItem())) {
						System.out.println("[REBORNCRAFTBYPASS] Farklı item bulundu, interactionManager ile tıklanıyor: slot=" + i + ", item=" + currentItem.getItem().toString());
						MinecraftClient mc = MinecraftClient.getInstance();
						ClientPlayerInteractionManager im = mc.interactionManager;
						im.clickSlot(containerScreen.getScreenHandler().syncId, i, 0, net.minecraft.screen.slot.SlotActionType.PICKUP, mc.player);
						mc.player.closeHandledScreen();
						return true;
					}
				}
			} else {
				System.out.println("[REBORNCRAFTBYPASS] Referans item bulunamadı!");
			}
		}
		return false;
	}
}