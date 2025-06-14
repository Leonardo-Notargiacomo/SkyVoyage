<!-- src/lib/components/DankMode.svelte -->
<div class="text-center mb-6 dank-banner">
    <p class="rainbow-text text-2xl mb-4">😎 69% Discount Activated 😎</p>

    <!-- Center TURKIYE -->
    <img
            src="https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExbmdkdTFwbXB2dWdzMG44Z3d0bWdiYzB0dzVyNW5vbDNoMWtmY21rMyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/d6gXFbMVhWIJezJXP7/giphy.gif"
            alt="Turkiye"
            class="turkiye"
    />

    <!-- Flying Memes -->
    <img
            src="https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExZWljajB0MzVyYjY4ZzYxM244YWVzaTVzZjZxNG45c2Q2MWh2MDU3OSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/XqPOScfuIgn1C/giphy.gif"
            class="fly-gif one"
    />
    <img
            src="https://media.giphy.com/media/v9OLgAL8p8FOM/giphy.gif?cid=790b7611r2x7y06b18ksecmxdr1lotv3yiv6arsbaenpxnga&ep=v1_gifs_search&rid=giphy.gif&ct=g"
            class="fly-gif two"
    />
    <img
            src="https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExcHZwY3E4bmI5aGlldXZvbjJycHNjeWx3dGoyNnVpd2wzcGs5cTZzYiZlcD12MV9naWZzX3NlYXJjaCZjdD1n/3o6ZtdUnbbEEXexdGE/giphy.gif"
            class="fly-gif three"
    />
    <img
            src="/Barrelino-Sheeparino.png"
            alt="Barrelino Sheeparino"
            class="barrelino"
    />
</div>

<div class="dank-mode-container">
  <div class="dank-objects">
    <div class="dank-object">
      <img 
        src="/images/dank/doge.png" 
        alt="Doge meme" 
        class="dank-img doge" />
    </div>
    <div class="dank-object">
      <img 
        src="/images/dank/mlg-glasses.png" 
        alt="MLG glasses" 
        class="dank-img glasses" />
    </div>
    <div class="dank-object">
      <img 
        src="/images/dank/rainbow.png" 
        alt="Rainbow effect" 
        class="dank-img rainbow" />
    </div>
  </div>
</div>

<style>
    .dank-banner {
        animation: fadeIn 1s ease-in-out;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: scale(0.95);
        }
        to {
            opacity: 1;
            transform: scale(1);
        }
    }

    .rainbow-text {
        font-size: 2rem;
        font-weight: bold;
        background: linear-gradient(
                to right,
                red,
                orange,
                yellow,
                green,
                blue,
                indigo,
                violet
        );
        -webkit-background-clip: text;
        color: transparent;
        animation: rainbow 2s infinite linear;
    }

    @keyframes rainbow {
        0% {
            filter: hue-rotate(0deg);
        }
        100% {
            filter: hue-rotate(360deg);
        }
    }

    .turkiye {
        width: 180px;
        margin: 0 auto;
        border-radius: 12px;
        box-shadow: 0 0 15px limegreen;
        animation: popIn 1s ease-in-out;
    }

    .fly-gif {
        position: fixed;
        width: 100px;
        z-index: 50;
        opacity: 0.8;
        animation: float 10s linear infinite;
        pointer-events: none;
    }

    .fly-gif.one {
        top: 10%;
        left: -150px;
        animation-delay: 0s;
    }

    .fly-gif.two {
        top: 40%;
        left: -200px;
        animation-delay: 2s;
    }

    .fly-gif.three {
        top: 70%;
        left: -180px;
        animation-delay: 4s;
    }

    @keyframes float {
        0% {
            transform: translateX(0) rotate(0deg);
            opacity: 0.7;
        }
        50% {
            transform: translateX(110vw) rotate(360deg);
            opacity: 1;
        }
        100% {
            transform: translateX(0) rotate(720deg);
            opacity: 0.7;
        }
    }

    @keyframes popIn {
        from {
            opacity: 0;
            transform: scale(0.7);
        }
        to {
            opacity: 1;
            transform: scale(1);
        }
    }

    .barrelino {
        position: fixed;
        top: 16px;
        right: 32px;
        width: 240px;
        height: auto;
        z-index: 1000;
        pointer-events: none;
    }

    .dank-mode-container {
        position: relative;
        width: 100%;
        height: 100vh;
        overflow: hidden;
    }

    .dank-objects {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        display: flex;
        justify-content: center;
        align-items: center;
        flex-wrap: wrap;
    }

    .dank-object {
        margin: 10px;
        animation: slideIn 1s ease-in-out;
    }

    @keyframes slideIn {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .dank-img {
        max-width: 100px;
        height: auto;
        display: block;
        margin: 0 auto;
    }

    .doge {
        animation: spin 10s linear infinite;
    }

    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }
        100% {
            transform: rotate(360deg);
        }
    }
</style>
