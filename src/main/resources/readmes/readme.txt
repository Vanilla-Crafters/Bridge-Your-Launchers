Server and Client Usage
Command:

/bridge <player> "Your Profile Name"
Profile Path:

.minecraft\config\bridgeyourlaunchers\profiles\Your Profile Name

    Note: You have to create your own profiles, as the mod does not provide pre-named profiles. This allows for full customization.

Setup Steps:

    Create Your First Profile:
        After creating your first profile as "Your Profile Name", you will be able to select it using in-game command suggestions.

    Adding a CurseForge Mod Profile Shortcut:
        Place your CurseForge mod profile shortcut into the "Your Profile Name" folder. Once this is done, you can use it in-game via the /bridge command.

    Profile Setup

    Using the /bridge Command:
        /bridge <player> "Your Profile Name"

Once the /bridge command is executed, the mod will:

    Close your game instance.
    Launch the provided shortcut.

Pay Attention! (Server-Side)
This part is in Progress. Please share your thoughts with us!

    Profile Naming:
        While creating profiles, ensure that the profile names on both the server-side and client-side are have to be same.

    Client:
    Profile Example

    Server:
    Profile Example

    Shortcut on Client:
    Place the CurseForge mod profile shortcut in the client-side profile folder.
        You do not need to add shortcuts to server-side profiles, as shortcuts are only designed for clients at the moment. Server-side features may be added in the future.

Detection for developers:

Developers can customize commands by modifying the config_commands.json file located in:

.minecraft\config\bridgeyourlaunchers\

Simply if .url (shortcut) found execute this. If not found, execute this. (Server-Side or whoever hosts the world)