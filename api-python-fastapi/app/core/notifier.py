import httpx
from app.config import settings

async def notify_device_creation(device_id: str, device_name: str, device_type: str):
    if not settings.DISCORD_WEBHOOK_URL:
        print("Notification skipped: DISCORD_WEBHOOK_URL is not set.")
        return

    payload = {
        "embeds": [
            {
                "title": "New Device Registered",
                "description": "A new infrastructure asset has been registered into the core ecosystem.",
                "color": 3066993,
                "fields": [
                    {
                        "name": "Device UUID",
                        "value": f"`{device_id}`",
                        "inline": False
                    },
                    {
                        "name": "Device Name",
                        "value": device_name,
                        "inline": True
                    },
                    {
                        "name": "Device Type",
                        "value": f"`{device_type}`",
                        "inline": True
                    }
                ],
                "footer": {
                    "text": "Shoutout to Python Backend"
                }
            }
        ]
    }

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(settings.DISCORD_WEBHOOK_URL, json=payload)
            response.raise_for_status()
    except Exception as e:
        print(f"Failed to deliver Discord notification webhook payload: {e}")
