# Privacy Policy for Trackify

**Last updated:** October 12, 2026

Trackify ("we", "our", or "the app") is committed to protecting your privacy. This Privacy Policy explains our practices regarding data collection and usage.

## 1. No Data Collection
Trackify is an offline, local-first application designed to help you manage your subscriptions and recurring payments.
- **We do not collect, transmit, store, or sell any personal data.**
- We do not use third-party analytics, tracking services, advertising SDKs, or cloud synchronization.

## 2. Local Storage
All information entered into Trackify (such as subscription names, prices, categories, and payment history) is stored exclusively on your device's local database (SQLite via Android Room).

## 3. Data Backups, Exports & Diagnostic Logs
- If you use the backup or export feature, the data is saved as a local JSON file directly on your device via the Android Storage Access Framework (SAF).
- We do not have access to your backups or exported files.
- Application data is excluded from automatic system cloud backups to protect your privacy and ensure database integrity.
- Diagnostic crash logs (containing app version, Android version, device model, and error stack traces) are stored strictly locally in the app's private storage. They are never transmitted automatically and are shared only if you manually choose "Send log to developer" via the Android system share sheet.

## 4. Notifications & Permissions
- **Notifications (`POST_NOTIFICATIONS`):** Used solely to schedule local reminders on your device for upcoming subscription renewals and expiration dates.
- **Boot Completed (`RECEIVE_BOOT_COMPLETED`):** Used solely to re-arm your local reminder alarms after your device restarts.

## 5. Contact & Support
If you have questions or concerns about this Privacy Policy, you can reach out via:
- GitHub: [https://github.com/onlive1337/Trackify](https://github.com/onlive1337/Trackify)
- Telegram: [https://t.me/onswix](https://t.me/onswix)

---

# Политика конфиденциальности Trackify

**Дата обновления:** 12 октября 2026 г.

Trackify уважает вашу конфиденциальность и обеспечивает полную защиту ваших данных.

## 1. Отсутствие сбора данных
Trackify — это полностью локальное приложение для учета подписок и регулярных платежей.
- **Мы не собираем, не передаем, не храним и не продаем никакие персональные данные.**
- В приложении отсутствуют сторонние трекеры, аналитика, реклама и облачная синхронизация.

## 2. Локальное хранение
Все данные (названия подписок, суммы, даты, категории и заметки) хранятся исключительно локально на вашем устройстве в базе данных SQLite.

## 3. Экспорт, резервное копирование и диагностические логи
- Резервные копии создаются в виде JSON-файла напрямую через стандартный механизм выбора файлов Android (Storage Access Framework).
- Доступ к файлам бэкапа имеет только пользователь.
- Диагностические логи ошибок (версия приложения, версия Android, модель устройства и стектрейс ошибки) сохраняются исключительно локально во внутренней памяти приложения. Они никогда не отправляются автоматически и могут быть переданы только вручную самим пользователем через кнопку «Отправить лог разработчику» и системное меню «Поделиться».

## 4. Разрешения
- **Уведомления (`POST_NOTIFICATIONS`):** используются исключительно для локальных напоминаний о предстоящих платежах.
- **Автозапуск после перезагрузки (`RECEIVE_BOOT_COMPLETED`):** используется исключительно для восстановления локальных будильников напоминаний после перезагрузки смартфона.

## 5. Контакты
По всем вопросам вы можете связаться:
- GitHub: [https://github.com/onlive1337/Trackify](https://github.com/onlive1337/Trackify)
- Telegram: [https://t.me/onswix](https://t.me/onswix)
