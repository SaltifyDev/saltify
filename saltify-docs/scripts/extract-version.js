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

// 替换 content/index.md 中的版本号
const indexPath = path.join(__dirname, '../content/index.md');
let indexContent = fs.readFileSync(indexPath, 'utf8');

indexContent = indexContent.replace(
  /"org\.ntqqrev:saltify-core:[^"]+"/g,
  `"org.ntqqrev:saltify-core:${version}"`
);

fs.writeFileSync(indexPath, indexContent, 'utf8');
