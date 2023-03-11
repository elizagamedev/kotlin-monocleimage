{
  description = "Dev shell flake for monocleimage";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  inputs.flake-compat = {
    url = "github:edolstra/flake-compat";
    flake = false;
  };

  outputs = { self, nixpkgs, ... }:
    let
      pkgs = import nixpkgs {
        system = "x86_64-linux";
        config.allowUnfree = true;
      };
    in
    with pkgs;
    {
      devShells.x86_64-linux.default = mkShell {
        buildInputs = [
          kotlin
          gradle
          kotlin-language-server
        ];
        JAVA_HOME = "${jdk11}";
      };
    };
}
