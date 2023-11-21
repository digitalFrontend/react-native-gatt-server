module.exports = {
    env: {
        node: true,
        es2021: true,
        jest: true,
        browser: true,
        'react-native/react-native': true
    },
    extends: ['eslint:recommended', 'plugin:react/recommended', 'prettier'],
    parser: '@babel/eslint-parser',
    parserOptions: {
        ecmaFeatures: {
            jsx: true,
            globalReturn: true
        },
        ecmaVersion: 'latest'
    },
    plugins: ['react', 'unused-imports', 'react-native', 'prettier'],
    rules: {
        /* 'react/jsx-indent': ['error', 'tab'], //подсветка кривых отступов */
        /* 'react/jsx-indent-props': ['error', 'tab'], //подсветка кривых отступов */
        'linebreak-style': 0, //конфигурирование перевода строки LF / CRLF
        quotes: ['error', 'single', { avoidEscape: true }], //настройка на одинарные кавычки
        semi: ['error', 'never'], //выключение проверки ; в конце строки
        'no-unused-vars': 'warn', //подсветка не используемых переменных (только предупреждение, потому что капец скок по проекту смотреть надо)
        'unused-imports/no-unused-imports': 'error', //помогает чистить неиспользуемые импорты
        'unused-imports/no-unused-vars': [
            'off',
            {
                vars: 'all',
                varsIgnorePattern: '^_',
                args: 'after-used',
                argsIgnorePattern: '^_'
            }
        ], //помогает чистить неиспользуемые переменные, но чет не чистит
        'react/prop-types': 'off', //выключаем необходимость задавать типы пропсов, потому что мы это не делаем
        'react/display-name': 'off', //выключаем необходимость именовать компоненты, потому что мы это не делаем
        'no-undef': 'off', //оставляем возможность задавать глобальные переменные, с включенным ругается, если в начале файла нет /*global */
        'no-empty': ['warn', { allowEmptyCatch: true }], //подсветка пустых if-else statement
        'no-useless-escape': 'off', //оставил возможность оставлять ненужные литералы, потому что нам надо, а по мнению линтера это не правильно
        'no-empty-pattern': 'off', //оставил возможность пихать пустые функции или пустые деструктуризации объектов
        'no-console': ['warn'],
        'no-tabs': 1, //линтер по умолчанию не любит табы, поэтому выключил
        trailingComma: 'off', //отключил обязательную запяту в конце перечисления (в prettier тоже выключено)
        /* 'react/jsx-key': 'off', */ //дает возможность отключит обязательную выдачу ключей компонентам
        'no-unreachable': ['warn'], //подсвечивает недостижимые участки кода
        'no-loss-of-precision': 'off', //эта штука не дает всякие дикие числа писать, но где-то стреляла в нужном месте, поэтому выключено
        'no-case-declarations': 'off', //запрещает создавать переменные в switch case конструкции, но у нас это чудо есть, потому выключено
        'no-fallthrough': 'off', //запрещает сработку нескольких кейсов в switch case, но у нас опять же где-то было
        /* 'no-mixed-spaces-and-tabs': 'off', */ //не дает миксовать пробелы и табы, но вроде как щас все ок должно быть
        'implicit-arrow-linebreak': ['error'], //по сути не дает переходить на другую строку, если нет return в стрелочной функции, но я оставил возможность при переходе до выражения
        'react-native/no-unused-styles': ['warn'] //подсвечивает неиспользуемые стили
    },
    ignorePatterns: ['**/*.e2e.js'],
    globals: {
        var1: 'readonly'
    },
    settings: {
        react: {
            version: 'detect'
        }
    }
}
