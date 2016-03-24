#!/usr/bin/ruby

require 'json'

if ARGV.empty?
  puts "Missing program arguments, please specify file path, server host and rows counter e.g - /tmp/register-installations-siege-data.txt localhost 10000"
  exit
end

PATH = ARGV[0]
HOST = ARGV[1]
COUNTER = ARGV[2]

puts "Opening output file #{PATH} for #{HOST} #{COUNTER} rows!"

target = open("#{PATH}", 'w')

puts "Truncating the file.  Goodbye!"
target.truncate(0)
