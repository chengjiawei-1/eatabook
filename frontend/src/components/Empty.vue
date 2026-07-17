<template>
  <div :class="cn('flex h-full items-center justify-center')" @click="() => toast('Coming soon')">
    Empty
  </div>
</template>

<script setup lang="ts">
import { toast } from "vue-sonner";
import { cn } from "@/lib/utils";
</script>

