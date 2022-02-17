fs = require('fs')

function usage(){
  console.log("Usage: qq [in-file] [out-file (optional)]")
  console.log("")
  console.log("  in-file must contain one or more built-in prompt objects as json.")
  console.log("  See:  https://github.com/enquirer/enquirer#-built-in-prompts")
  process.exit(1)
}

// all types listed here:
// https://github.com/enquirer/enquirer/blob/8d626c206733420637660ac7c2098d7de45e8590/lib/prompts/index.js

function readJson(j){
  try { return JSON.parse(j) } catch(error) { usage() }
}

const { prompt } = require('enquirer');

const in_file = process.argv[2] || 'in.json'
const out_file = process.argv[3] || 'out.json'

fs.readFile(in_file,
            'utf8',
            function(err, data)
            {
              const question = readJson(data)
              prompt(question)
                .then(answer => fs.writeFile(out_file, JSON.stringify(answer), null, x => x))
                .catch(x => x)})
