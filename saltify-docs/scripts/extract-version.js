const fs = require('fs');
const path = require('path');

// 从 build.gradle.kts 读取版本号
const gradleFile = path.join(__dirname, '../../build.gradle.kts');
const content = fs.readFileSync(gradleFile, 'utf8');

const versionMatch = content.match(/version\s*=\s*"([^"]+)"/);
const version = versionMatch ? versionMatch[1] : 'unknown';

const outputPath = path.join(__dirname, '../app/version.ts');
const output = `export const SALTIFY_VERSION = '${version}';`;

fs.writeFileSync(outputPath, output, 'utf8');
