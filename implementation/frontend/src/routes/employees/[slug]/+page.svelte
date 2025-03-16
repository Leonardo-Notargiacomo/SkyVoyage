<script>
    import {onMount} from "svelte";
    import {api} from "$lib/api.js";
    import { page } from '$app/state';

    let employee = $state({});

    onMount(() => {
        load()
    });

    const load = async () => {
        try{
            employee = await api.one("employees", page.params.slug);
        }catch (e) {
            // Handle the error
            console.log(e);
        }
    }
</script>

<h1 class="text-4xl">Employee</h1>

Do something with the employee data here.