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
    class="inline-flex items-center p-2 mt-2 ms-3 text-sm text-gray-600 rounded-lg sm:hidden hover:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-300 transition-all duration-200"
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
      class="flex flex-col h-full py-5 overflow-y-auto bg-white shadow-lg border-r border-gray-100"
    >
      <!-- Logo/Brand -->
      <div class="px-6 mb-6">
        <div class="flex items-center gap-3">
          <img
                  src="/SkyVoyage-square.png"
                  alt="SkyVoyage Logo"
                  class="w-10 h-10 rounded-xl shadow-lg object-cover border border-gray-200"
          />
          <span class="text-xl font-extrabold tracking-tight text-gray-800">SkyVoyage</span>
        </div>
      </div>

      <div class="px-3 mb-6">
        <span class="px-4 text-xs font-semibold text-gray-400 uppercase tracking-wider"
          >Main Menu</span
        >
      </div>

      <ul class="space-y-1 px-3">
        <li>
          <a
            href="/home"
            class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
          >
            <div class="bg-blue-50 p-2 rounded-lg mr-3 text-blue-600 group-hover:bg-blue-100 transition-colors duration-200">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="feather feather-home"
              >
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9 22 9 12 15 12 15 22"></polyline>
              </svg>
            </div>
            <span class="font-medium">Dashboard</span>
          </a>
        </li>
        {#if type === "Sales Manager" || type === "Account Manager"}
          <li>
            <a
              href="/admin"
              class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
            >
              <div class="bg-blue-50 p-2 rounded-lg mr-3 text-blue-600 group-hover:bg-blue-100 transition-colors duration-200">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-dollar-sign"
                >
                  <line x1="12" y1="1" x2="12" y2="23"></line>
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
                </svg>
              </div>
              <span class="font-medium">Price Management</span>
            </a>
          </li>
          <li>
            <a
              href="/admin/discounts"
              class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
            >
              <div class="bg-blue-50 p-2 rounded-lg mr-3 text-blue-600 group-hover:bg-blue-100 transition-colors duration-200">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-percent"
                >
                  <line x1="19" y1="5" x2="5" y2="19"></line>
                  <circle cx="6.5" cy="6.5" r="2.5"></circle>
                  <circle cx="17.5" cy="17.5" r="2.5"></circle>
                </svg>
              </div>
              <span class="font-medium">Discount Management</span>
            </a>
          </li>
        {/if}
        <li>
          {#if type !== "Sales Employee"}
            <a
              href="/employees"
              class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
            >
              <div class="bg-blue-50 p-2 rounded-lg mr-3 text-blue-600 group-hover:bg-blue-100 transition-colors duration-200">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-users"
                >
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              </div>
              <span class="font-medium">Employees</span>
            </a>
          {/if}
        </li>
        {#if type !== "Sales Employee"}
          <li>
            <a
              href="/kpiDashboard"
              class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
            >
              <div class="bg-blue-50 p-2 rounded-lg mr-3 text-blue-600 group-hover:bg-blue-100 transition-colors duration-200">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  class="feather feather-bar-chart"
                >
                  <line x1="12" y1="20" x2="12" y2="10"></line>
                  <line x1="18" y1="20" x2="18" y2="4"></line>
                  <line x1="6" y1="20" x2="6" y2="16"></line>
                </svg>
              </div>
              <span class="font-medium">KPI Dashboard</span>
            </a>
          </li>
          {#if type === "Sales Manager" || type === "Account Manager"}
          <li>
              <a
                      href="/sales/bookings"
                      class="flex items-center px-4 py-3 text-gray-700 rounded-lg hover:bg-blue-50 hover:text-blue-700 transition-all duration-200 group"
              >
                <div class="bg-blue-50 p-2 rounded-lg mr-3 text-blue-600 group-hover:bg-blue-100 transition-colors duration-200">
                  <svg
                          xmlns="http://www.w3.org/2000/svg"
                          width="18"
                          height="18"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          stroke-width="2"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          class="feather feather-clipboard"
                  >
                    <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"></path>
                    <rect x="8" y="2" width="8" height="4" rx="1" ry="1"></rect>
                  </svg>
                </div>
                <span class="font-medium">My Bookings</span>
              </a>
            </li>
          {/if}
        {/if}
      </ul>

      <!-- User Profile Section -->
      <div class="mt-auto pt-6 px-3">
        <div class="px-3 mb-2">
          <span class="text-xs font-semibold text-gray-400 uppercase tracking-wider"
            >Account</span
          >
        </div>

        <div id="user-profile" class="relative px-3">
          <div
            class="flex items-center gap-3 cursor-pointer hover:bg-blue-50 p-3 rounded-lg transition-all duration-200"
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
              class="relative flex-shrink-0 inline-flex items-center justify-center w-10 h-10 overflow-hidden bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg text-white shadow-md"
            >
              <span class="font-bold text-sm">{initials}</span>
            </div>
            <div class="flex-grow">
              <div class="flex items-center justify-between">
                <div class="font-medium text-gray-800 truncate max-w-[140px]">
                  {firstname + " " + lastname}
                </div>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4 text-gray-400 ml-1"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  />
                </svg>
              </div>
              <div class="text-xs text-gray-500">{type}</div>
            </div>
          </div>

          {#if dropdownOpen}
            <div
              class="absolute left-1/2 bottom-full mb-2 w-48 bg-white rounded-lg shadow-xl border border-gray-100 z-50 origin-bottom transition-all duration-200 ease-out animate-fadeIn flex flex-col items-center"
              style="transform: translateX(-50%);"
            >
              <div class="p-2 w-full flex flex-col items-center">
                <button
                  on:click={logout}
                  class="w-full text-left px-3 py-2.5 rounded-lg hover:bg-red-50 transition-colors duration-200 flex items-center group justify-center"
                >
                  <div
                    class="bg-red-50 group-hover:bg-red-100 p-2 rounded-lg mr-3 text-red-600 transition-colors duration-200"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="w-4 h-4"
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
                  <span class="font-medium text-gray-700 group-hover:text-red-600 transition-colors duration-200">
                    Logout
                  </span>
                </button>
              </div>

              <!-- Tooltip Arrow -->
              <div
                class="absolute left-1/2 bottom-0 transform -translate-x-1/2 translate-y-full"
              >
                <div
                  class="border-t-8 border-l-8 border-r-8 border-t-white border-l-transparent border-r-transparent"
                ></div>
              </div>
            </div>
          {/if}
        </div>
      </div>
    </div>
  </aside>
{/if}

<div class="app-background">
  <div
    class="p-4 {$page.url.pathname !== '/' && $page.url.pathname !== '/login'
      ? 'sm:ml-64'
      : ''}"
  >
    <slot />
  </div>
</div>

<style>
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translate(-50%, 10px);
    }
    to {
      opacity: 1;
      transform: translate(-50%, 0);
    }
  }

  .animate-fadeIn {
    animation: fadeIn 0.2s ease-out forwards;
  }

  /* Modern background styling */
  .app-background {
    min-height: 100vh;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);
    background-attachment: fixed;
    position: relative;
    overflow: hidden;
  }

  .app-background::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      radial-gradient(circle at 25% 25%, rgba(255, 255, 255, 0.2) 0%, transparent 50%),
      radial-gradient(circle at 75% 75%, rgba(255, 255, 255, 0.2) 0%, transparent 50%);
    z-index: 0;
  }

  .app-background::after {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%2393c5fd' fill-opacity='0.1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
    z-index: 0;
    opacity: 0.5;
  }

  /* Ensure content is above the background */
  .app-background > div {
    position: relative;
    z-index: 1;
  }

  /* Add a subtle animation to make the UI feel more alive */
  @keyframes subtlePulse {
    0%, 100% { background-position: 0% 50%; }
    50% { background-position: 100% 50%; }
  }
</style>
