const { loadEnvFile } = require('node:process');
loadEnvFile();

async function sendDeviceCreatedAlert(device) {
    const webhookUrl = process.env.DISCORD_WEBHOOK_URL;
    
    // Gracefully exit if webhook isn't configured yet
    if (!webhookUrl) {
        console.warn('[Discord Webhook Warning]: DISCORD_WEBHOOK_URL is not defined in environment.');
        return;
    }

    const payload = {
        embeds: [
            {
                title: "New Device Registered",
                description: `A new hardware node has successfully joined the telemetry pipeline.`,
                color: 3066993,
                fields: [
                    {
                        name: "Device UUID",
                        value: `\`${device.id}\``,
                        inline: false
                    },
                    {
                        name: "Device Name",
                        value: `\`${device.name}\``,
                        inline: true
                    },
                    {
                        name: "Device Type",
                        value: `\`${device.type}\``,
                        inline: true
                    }
                ],
                footer: {
                    text: "Shoutout to Node.js Backend"
                }
            }
        ]
    };

    try {
        const response = await fetch(webhookUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`Discord API responded with status ${response.status}`);
        }

        console.log(`[Discord Webhook]: Alert delivered for device ${device.id}`);
    } catch (err) {
        console.error(`[Discord Webhook Error]: Failed to dispatch alert: ${err.message}`);
    }
}

module.exports = { sendDeviceCreatedAlert };
