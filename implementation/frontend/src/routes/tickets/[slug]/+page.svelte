<script>
  import { onMount } from "svelte";
  import { api } from "$lib/api.js";
  import { page } from "$app/stores";
  import { goto } from "$app/navigation";
    import { loadSvelteConfig } from "@sveltejs/vite-plugin-svelte";
    
  let ticket = $state({});
  let message = $state("");
  let error = $state("");
  let isLoading = $state(true);

  onMount(() => {
    loadSvelteConfig();
  });

  const load = async () => {
    try {
      isLoading = true;
      tickets = await api.list("tickets", $page.params.slug);
      isLoading = false;
    } catch (e) {
      isLoading = false;
      console.log(e);
    }
  };
</script>