require 'rubygems'
require 'bundler/setup'
require 'monkey'
require 'swagger'
require 'vcr'
require 'typhoeus'
require 'json'
require 'yaml'
require 'rspec'

Dir[File.join(File.dirname(__FILE__), "../lib/*.rb")].each {|file| require file }
Dir[File.join(File.dirname(__FILE__), "../models/*.rb")].each {|file| require file }
Dir[File.join(File.dirname(__FILE__), "../resources/*.rb")].each {|file| require file }

RSpec.configure do |config|
  # some (optional) config here
end

VCR.config do |config|
  config.cassette_library_dir = 'spec/vcr'
  config.stub_with :webmock # or :fakeweb
end

WebMock.allow_net_connect! if defined? WebMock

def help
  puts "\nOh noes! You gotta stuff your swagger credentials in ~/.swagger.yml like so:\n\n"
  puts "api_key: '12345abcdefg'"
  puts "username: 'fumanchu'"
  puts "password: 'kalamazoo'\n\n"
  exit
end

# Parse ~/.swagger.yml for user credentials
# begin
  CREDENTIALS = YAML::load_file(File.join(ENV['HOME'], ".swagger.yml")).symbolize_keys
# rescue
  # help
# end

help unless Object.const_defined? 'CREDENTIALS'
help unless [:api_key, :username, :password].all? {|key| CREDENTIALS[key].present? }

def configure_swagger
  Swagger.configure do |config|
    config.api_key = CREDENTIALS[:api_key]
    config.username = CREDENTIALS[:username]
    config.password = CREDENTIALS[:password]

    config.host = 'petstore.swagger.wordnik.com'
    config.base_path = '/api'
  end
end

configure_swagger

def sample_resource_body
  @sample_resource_body ||= begin
    File.open(File.join(File.dirname(__FILE__), "../api_docs/word.json"), "r").read
  end
end

# A random string to tack onto stuff to ensure we're not seeing 
# data from a previous test run
RAND = ("a".."z").to_a.sample(8).join