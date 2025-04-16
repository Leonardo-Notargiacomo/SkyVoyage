<script>
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import "../app.css";

  let firstname = "";
  let lastname = "";
  let type = "";
  let initials = "";
  let activeUser = false;
  let dropdownOpen = false;
  let userMenuTransition = false;

  function logout() {
    // Clear cookies
    document.cookie =
      "firstname=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
    document.cookie =
      "lastname=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";
    document.cookie = "type=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;";

    // Notify layout to clear user info
    window.dispatchEvent(new Event("userInfoChanged"));

    // Redirect to login
    goto("/");
  }

  function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(";").shift();
    return "";
  }

  function updateUserInfo() {
    firstname = getCookie("firstname") || "";
    lastname = getCookie("lastname") || "";
    const rawType = getCookie("type") || "";

    const roleLabels = {
      "1": "Sales Manager",
      "2": "Sales Employee",
      "3": "Account Manager",
    };

    type = roleLabels[rawType] || rawType;

    initials =
      (firstname.charAt(0) || "").toUpperCase() +
      (lastname.charAt(0) || "").toUpperCase();
  }

  function toggleDropdown() {
    dropdownOpen = !dropdownOpen;
  }

  function handleClickOutside(event) {
    const userProfile = document.getElementById("user-profile");
    if (userProfile && !userProfile.contains(event.target)) {
      dropdownOpen = false;
    }
  }

  onMount(() => {
    updateUserInfo();

    if (activeUser === false) {
      // Check if user is logged in
      const userType = getCookie("type");
      if (!userType) {
        goto("/");
      }
    }

    // Listen for changes after login
    window.addEventListener("userInfoChanged", updateUserInfo);

    // Add event listener for clicks outside dropdown
    document.addEventListener("click", handleClickOutside);

    return () => {
      window.removeEventListener("userInfoChanged", updateUserInfo);
      document.removeEventListener("click", handleClickOutside);
    };
  });
</script>

{#if $page.url.pathname !== "/"}
  <button
    data-drawer-target="default-sidebar"
    data-drawer-toggle="default-sidebar"
    aria-controls="default-sidebar"
    type="button"
    class="inline-flex items-center p-2 mt-2 ms-3 text-sm text-gray-500 rounded-lg sm:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600"
  >
    <span class="sr-only">Open sidebar</span>
    <svg
      class="w-6 h-6"
      aria-hidden="true"
      fill="currentColor"
      viewBox="0 0 20 20"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        clip-rule="evenodd"
        fill-rule="evenodd"
        d="M2 4.75A.75.75 0 012.75 4h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 4.75zm0 10.5a.75.75 0 01.75-.75h7.5a.75.75 0 010 1.5h-7.5a.75.75 0 01-.75-.75zM2 10a.75.75 0 01.75-.75h14.5a.75.75 0 010 1.5H2.75A.75.75 0 012 10z"
      ></path>
    </svg>
  </button>

  <aside
    id="default-sidebar"
    class="fixed top-0 left-0 z-40 w-64 h-screen transition-transform -translate-x-full sm:translate-x-0"
    aria-label="Sidebar"
  >
    <div
      class="flex flex-col h-full px-3 py-4 overflow-y-auto bg-gray-50 dark:bg-gray-800"
    >
      <ul class="space-y-2 font-medium">
        <li>
          <a
            href="/home"
            class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              class="feather feather-home"
              ><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
              ></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg
            >
            <span class="flex-1 ms-3 whitespace-nowrap">Dashboard</span>
          </a>
        </li>
        <!-- <li>
                <a href="/customers" class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-users"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                    <span class="flex-1 ms-3 whitespace-nowrap">Customers</span>
                </a>
            </li> -->

        <li>
          {#if type !== "Sales Employee"}
            <a
              href="/employees"
              class="flex items-center p-2 text-gray-900 rounded-lg dark:text-white hover:bg-gray-100 dark:hover:bg-gray-700 group"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-users"
                ><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"
                ></path><circle cx="9" cy="7" r="4"></circle><path
                  d="M23 21v-2a4 4 0 0 0-3-3.87"
                ></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg
              >
              <span class="flex-1 ms-3 whitespace-nowrap">Employees</span>
            </a>
          {/if}
        </li>
      </ul>
      <div
        class="mt-auto pt-4 space-y-2 font-medium border-t border-gray-200 dark:border-gray-700"
      >
        <div id="user-profile" class="relative">
          <div
            class="flex items-center gap-4 cursor-pointer hover:bg-gray-100 dark:hover:bg-gray-700 p-3 rounded-lg transition-colors duration-200"
            on:click={toggleDropdown}
            on:keydown={(event) => {
              if (event.key === "Enter" || event.key === " ") {
                event.preventDefault();
                toggleDropdown();
              }
            }}
            role="button"
            tabindex="0"
            aria-haspopup="true"
            aria-expanded={dropdownOpen}
          >
            <div
              class="relative flex-shrink-0 inline-flex items-center justify-center w-12 h-12 overflow-hidden bg-gradient-to-br from-blue-700 to-blue-900 rounded-full text-white shadow-md mr-2"
            >
              <span class="font-bold text-sm">
              {initials}
              </span>
            </div>
            <div class="flex-grow font-medium dark:text-white">
              <div class="flex items-center justify-between">
              <div class="truncate max-w-[140px]">
                {firstname + " " + lastname}
              </div>
              </div>
              <div class="text-sm text-gray-500 dark:text-gray-400">{type}</div>
            </div>
          </div>

          {#if dropdownOpen}
            <div
              class="absolute left-1/2 bottom-full mb-3 transform -translate-x-1/2 w-48 bg-white rounded-lg shadow-xl border border-gray-200 dark:bg-gray-800 dark:border-gray-700 z-50 origin-bottom transition-all duration-200 ease-out"
              style="box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);"
            >
              <div class="p-2">
                <button
                  on:click={logout}
                  class="w-full text-left px-3 py-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors duration-200 flex items-center group"
                >
                  <div
                    class="bg-red-100 group-hover:bg-red-200 p-2 rounded-lg mr-3 text-red-600 transition-colors duration-200"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="w-5 h-5"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                      />
                    </svg>
                  </div>
                  <span
                    class="font-medium text-gray-700 dark:text-gray-200 group-hover:text-red-600 dark:group-hover:text-red-400 transition-colors duration-200"
                  >
                    Logout
                  </span>
                </button>
              </div>

              <!-- Tooltip Arrow -->
              <div
                class="absolute left-1/2 bottom-0 transform -translate-x-1/2 translate-y-full"
              >
                <div
                  class="border-t-8 border-l-8 border-r-8 border-t-white dark:border-t-gray-800 border-l-transparent border-r-transparent"
                ></div>
              </div>
            </div>
          {/if}
        </div>
      </div>
    </div>
  </aside>
{/if}

<div
  class="p-4 {$page.url.pathname !== '/' && $page.url.pathname !== '/login'
    ? 'sm:ml-64'
    : ''}"
>
  <slot />
</div>
